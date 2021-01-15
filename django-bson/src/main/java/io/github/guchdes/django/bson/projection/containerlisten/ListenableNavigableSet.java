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

import java.util.Iterator;
import java.util.NavigableSet;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class ListenableNavigableSet<E> extends ListenableSortedSet<E> implements NavigableSet<E> {
    public ListenableNavigableSet(NavigableSet<E> set, SetListener<E> listener) {
        super(set, listener);
    }

    /**
     * @param isSync 是否对操作collection和调用listener加锁
     */
    public ListenableNavigableSet(NavigableSet<E> set, SetListener<E> listener, boolean isSync) {
        super(set, listener, isSync);
    }

    ListenableNavigableSet(NavigableSet<E> set, SetListener<E> listener, ChangeLock changeLock, Object lock) {
        super(set, listener, changeLock, lock);
    }

    public SetListener<E> getListener() {
        return (SetListener<E>) listener;
    }

    private NavigableSet<E> getSet() {
        return (NavigableSet<E>) collection;
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ListenableNavigableSet<>(getSet().descendingSet(), new ViewSetListener(), changeLock, lock);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return monitorIterator(getSet().descendingIterator());
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new ListenableNavigableSet<>(getSet().subSet(fromElement, fromInclusive, toElement, toInclusive),
                new ViewSetListener(), changeLock, lock);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ListenableNavigableSet<>(getSet().headSet(toElement, inclusive), new ViewSetListener(), changeLock, lock);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ListenableNavigableSet<>(getSet().tailSet(fromElement, inclusive), new ViewSetListener(), changeLock, lock);
    }

    @Override
    public E lower(E e) {
        return getSet().lower(e);
    }

    @Override
    public E floor(E e) {
        return getSet().floor(e);
    }

    @Override
    public E ceiling(E e) {
        return getSet().ceiling(e);
    }

    @Override
    public E higher(E e) {
        return getSet().higher(e);
    }

    @Override
    public E pollFirst() {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                E r = getSet().pollFirst();
                if (hasLock && r != null) {
                    getListener().afterRemove(getThis(), r);
                }
                return r;
            });
        });
    }

    @Override
    public E pollLast() {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                E r = getSet().pollLast();
                if (hasLock && r != null) {
                    getListener().afterRemove(getThis(), r);
                }
                return r;
            });
        });
    }
}
