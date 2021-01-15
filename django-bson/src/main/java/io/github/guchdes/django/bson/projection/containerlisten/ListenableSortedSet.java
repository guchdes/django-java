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

import java.util.Comparator;
import java.util.SortedSet;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class ListenableSortedSet<E> extends ListenableSet<E> implements SortedSet<E> {

    public SetListener<E> getListener() {
        return (SetListener<E>) listener;
    }

    private SortedSet<E> getSet() {
        return (SortedSet<E>) collection;
    }

    public ListenableSortedSet(SortedSet<E> set, SetListener<E> listener) {
        super(set, listener);
    }

    /**
     * @param isSync 是否对操作collection和调用listener加锁
     */
    public ListenableSortedSet(SortedSet<E> set, SetListener<E> listener, boolean isSync) {
        super(set, listener, isSync);
    }

    ListenableSortedSet(SortedSet<E> set, SetListener<E> listener, ChangeLock changeLock, Object lock) {
        super(set, listener, changeLock, lock);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return new ListenableSortedSet<>(getSet().subSet(fromElement, toElement), new ViewSetListener(), changeLock, lock);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return new ListenableSortedSet<>(getSet().headSet(toElement), new ViewSetListener(), changeLock, lock);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return new ListenableSortedSet<>(getSet().tailSet(fromElement), new ViewSetListener(), changeLock, lock);
    }

    @Override
    public Comparator<? super E> comparator() {
        return getSet().comparator();
    }

    @Override
    public E first() {
        return getSet().first();
    }

    @Override
    public E last() {
        return getSet().last();
    }

    protected class ViewSetListener extends ViewCollectionListener implements SetListener<E> {
    }

}
