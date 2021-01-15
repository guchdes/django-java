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
package io.github.guchdes.django.bson.projection;

import io.github.guchdes.django.bson.projection.containerlisten.ListenableNavigableMap;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

/**
 * 使用TreeMap实现
 *
 * @Author guch
 * @Since 3.0.0
 */
@NotThreadSafe
public class TreeDocumentMap<K, V> extends DocumentMap<K, V> implements NavigableMap<K, V> {

    public TreeDocumentMap() {
        super(node -> {
            return new ListenableNavigableMap<>(new TreeMap<>(), new ProxyMapListener<>(node), false);
        });
    }

    public TreeDocumentMap(NavigableMap<K, V> map) {
        super(node -> {
            return new ListenableNavigableMap<>(new TreeMap<>(map), new ProxyMapListener<>(node), false);
        });
    }

    ListenableNavigableMap<K,V> getMap() {
        return (ListenableNavigableMap<K, V>) map;
    }

    @SuppressWarnings("unchecked")
    public TreeMap<K, V> toTreeMap() {
        return (TreeMap<K, V>) deepCloneMap(TreeMap::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TreeDocumentMap<K, V> deepCloneSelf() {
        return (TreeDocumentMap<K, V>) super.deepCloneSelf();
    }

    /*代理方法*/

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return getMap().lowerEntry(key);
    }

    @Override
    public K lowerKey(K key) {
        return getMap().lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return getMap().floorEntry(key);
    }

    @Override
    public K floorKey(K key) {
        return getMap().floorKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return getMap().ceilingEntry(key);
    }

    @Override
    public K ceilingKey(K key) {
        return getMap().ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return getMap().higherEntry(key);
    }

    @Override
    public K higherKey(K key) {
        return getMap().higherKey(key);
    }

    @Override
    public Entry<K, V> firstEntry() {
        return getMap().firstEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
        return getMap().lastEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return getMap().pollFirstEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return getMap().pollLastEntry();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return getMap().descendingMap();
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return getMap().navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return getMap().descendingKeySet();
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return getMap().subMap(fromKey, fromInclusive, toKey, toInclusive);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return getMap().headMap(toKey, inclusive);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return getMap().tailMap(fromKey, inclusive);
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return getMap().subMap(fromKey, toKey);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return getMap().headMap(toKey);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return getMap().tailMap(fromKey);
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

    @Override
    public String toString() {
        return map.toString();
    }
}
