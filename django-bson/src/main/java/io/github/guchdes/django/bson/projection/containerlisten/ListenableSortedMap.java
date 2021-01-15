/**
 * MIT License
 *
 * Copyright (c) 2021 the original author or authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.guchdes.django.bson.projection.containerlisten;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

/**
*
 * @Author guch
 * @Since 3.0.0
 */
public class ListenableSortedMap<K, V> extends ListenableMap<K, V> implements SortedMap<K, V> {

    public ListenableSortedMap(SortedMap<K, V> map, MapListener<K, V> listener) {
        super(map, listener);
    }

    /**
     * @param isSync 是否对操作collection和调用listener加锁
     */
    public ListenableSortedMap(SortedMap<K, V> map, MapListener<K, V> listener, boolean isSync) {
        super(map, listener, isSync);
    }

    ListenableSortedMap(SortedMap<K, V> map, MapListener<K, V> listener, ChangeLock changeLock, Object lock) {
        super(map, listener, changeLock, lock);
    }

    private SortedMap<K, V> getMap() {
        return (SortedMap<K, V>) map;
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return new ListenableSortedMap<>(getMap().subMap(fromKey, toKey), new ViewMapListener(), changeLock, lock);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return new ListenableSortedMap<>(getMap().headMap(toKey), new ViewMapListener(), changeLock, lock);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return new ListenableSortedMap<>(getMap().tailMap(fromKey), new ViewMapListener(), changeLock, lock);
    }

    @Override
    public Comparator<? super K> comparator() {
        return getMap().comparator();
    }

    @Override
    public K firstKey() {
        return getMap().firstKey();
    }

    @Override
    public K lastKey() {
        return getMap().lastKey();
    }

    protected class ViewMapListener implements MapListener<K, V> {

        @Override
        public void afterPut(Map<K, V> map, K k, V v, V previous) {
            listener.afterPut(ListenableSortedMap.this, k, v, previous);
        }

        @Override
        public void afterRemove(Map<K, V> ignored, Object k, Object v) {
            listener.afterRemove(ListenableSortedMap.this, k, v);
        }

        @Nullable
        @Override
        public IncomingMapValueTransformer<K, V> getIncomingMapValueTransformer() {
            return listener.getIncomingMapValueTransformer();
        }

        @Override
        public void beforeClear(Map<K, V> container) {
            listener.beforeClear(ListenableSortedMap.this);
        }
    }
}
