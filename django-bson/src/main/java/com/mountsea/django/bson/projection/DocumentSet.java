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

import com.mountsea.django.bson.projection.containerlisten.HashSet2;
import com.mountsea.django.bson.projection.containerlisten.ListenableSet;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.function.Supplier;

/**
 * 使用HashSet做为实现类
 *
 * @author guch
 * @since 3.0.0
 */
@NotThreadSafe
public class DocumentSet<E> extends DocumentCollection<E> implements Set<E> {

    public DocumentSet() {
        this(node -> {
            return new ListenableSet<>(new HashSet2<>(), new ProxySetListener<>(node), false);
        });
    }

    public DocumentSet(Collection<? extends E> collection) {
        this(node -> {
            return new ListenableSet<>(new HashSet2<>(collection), new ProxySetListener<>(node), false);
        });
    }

    protected DocumentSet(ListenableCollectionCreator creator) {
        super(creator);
    }

    @SuppressWarnings("unchecked")
    public HashSet<E> cloneToHashSet() {
        return (HashSet<E>) deepCloneCollection(() -> new HashSet<>(size()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Set<E>> T cloneToSet(Supplier<T> listSupplier) {
        return (T) deepCloneCollection((Supplier<Collection<?>>) (Object) listSupplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DocumentSet<E> deepCloneSelf() {
        return super.deepCloneSelf();
    }

    Set<E> getSet() {
        return (Set<E>) collection;
    }

    @Override
    public int size() {
        return getSet().size();
    }

    @Override
    public boolean isEmpty() {
        return getSet().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getSet().contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return getSet().iterator();
    }

    @Override
    public Object[] toArray() {
        return getSet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getSet().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return getSet().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return getSet().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return getSet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return getSet().addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return getSet().retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return getSet().removeAll(c);
    }

    @Override
    public Spliterator<E> spliterator() {
        return getSet().spliterator();
    }

    @Override
    public String toString() {
        return collection.toString();
    }
}
