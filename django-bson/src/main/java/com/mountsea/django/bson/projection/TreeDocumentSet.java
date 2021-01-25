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
package com.mountsea.django.bson.projection;

import com.mountsea.django.bson.projection.containerlisten.ListenableNavigableSet;
import com.mountsea.django.bson.projection.containerlisten.TreeSet2;

import java.util.*;

/**
 * 使用TreeSet做为实现类
 *
 * @author guch
 * @since 3.0.0
 */
public class TreeDocumentSet<E> extends DocumentSet<E> implements NavigableSet<E> {

    public TreeDocumentSet() {
        this(node -> {
            return new ListenableNavigableSet<>(new TreeSet2<>(), new ProxySetListener<>(node), false);
        });
    }

    public TreeDocumentSet(Comparator<? super E> comparator) {
        this(node -> {
            return new ListenableNavigableSet<>(new TreeSet2<>(comparator), new ProxySetListener<>(node), false);
        });
    }

    public TreeDocumentSet(SortedSet<E> set) {
        this(node -> {
            return new ListenableNavigableSet<>(new TreeSet2<>(set), new ProxySetListener<>(node), false);
        });
    }

    protected TreeDocumentSet(ListenableCollectionCreator creator) {
        super(creator);
    }

    @SuppressWarnings("unchecked")
    public TreeSet<E> toTreeSet() {
        return (TreeSet<E>) deepCloneCollection(TreeSet::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TreeDocumentSet<E> deepCloneSelf() {
        return (TreeDocumentSet<E>) super.deepCloneSelf();
    }

    ListenableNavigableSet<E> getSet() {
        return (ListenableNavigableSet<E>) collection;
    }

    /* 代理方法 */

    public NavigableSet<E> descendingSet() {
        return getSet().descendingSet();
    }

    public Iterator<E> descendingIterator() {
        return getSet().descendingIterator();
    }

    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return getSet().subSet(fromElement, fromInclusive, toElement, toInclusive);
    }

    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return getSet().headSet(toElement, inclusive);
    }

    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return getSet().tailSet(fromElement, inclusive);
    }

    public E lower(E e) {
        return getSet().lower(e);
    }

    public E floor(E e) {
        return getSet().floor(e);
    }

    public E ceiling(E e) {
        return getSet().ceiling(e);
    }

    public E higher(E e) {
        return getSet().higher(e);
    }

    public E pollFirst() {
        return getSet().pollFirst();
    }

    public E pollLast() {
        return getSet().pollLast();
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
        return getSet().subSet(fromElement, toElement);
    }

    public SortedSet<E> headSet(E toElement) {
        return getSet().headSet(toElement);
    }

    public SortedSet<E> tailSet(E fromElement) {
        return getSet().tailSet(fromElement);
    }

    public Comparator<? super E> comparator() {
        return getSet().comparator();
    }

    public E first() {
        return getSet().first();
    }

    public E last() {
        return getSet().last();
    }

    @Override
    public String toString() {
        return collection.toString();
    }
}
