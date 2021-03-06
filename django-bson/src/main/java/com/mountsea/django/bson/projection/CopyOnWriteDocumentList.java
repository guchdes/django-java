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

import com.mountsea.django.bson.projection.containerlisten.ListenableCopyOnWriteArrayList;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * 使用CopyOnWriteArrayList存储元素的DocumentList
 *
 * @author guch
 * @since 3.0.0
 */
@ThreadSafe
public class CopyOnWriteDocumentList<E> extends DocumentCollection<E> implements List<E> {

    public CopyOnWriteDocumentList() {
        this(node -> {
            return new ListenableCopyOnWriteArrayList<>(new ProxyListListener<>(node));
        });
    }

    public CopyOnWriteDocumentList(Collection<? extends E> collection) {
        this(node -> {
            return new ListenableCopyOnWriteArrayList<>(collection, new ProxyListListener<>(node));
        });
    }

    protected CopyOnWriteDocumentList(ListenableCollectionCreator creator) {
        super(creator);
    }

    List<E> getList() {
        return (List<E>) collection;
    }

    //deepclone to ArrayList
    @SuppressWarnings("unchecked")
    public ArrayList<E> toArrayList() {
        return (ArrayList<E>) deepCloneCollection(() -> new ArrayList<>(size()));
    }

    //deepclone to List
    @SuppressWarnings("unchecked")
    public <T extends List<E>> T toList(Supplier<T> listSupplier) {
        return (T) deepCloneCollection((Supplier<Collection<?>>) (Object) listSupplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CopyOnWriteDocumentList<E> deepCloneSelf() {
        return super.deepCloneSelf();
    }

    /* 代理方法 */

    @Override
    public int size() {
        return getList().size();
    }

    @Override
    public boolean isEmpty() {
        return getList().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getList().contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return getList().iterator();
    }

    @Override
    public Object[] toArray() {
        return getList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getList().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return getList().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return getList().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return getList().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return getList().addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return getList().addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return getList().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return getList().retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        getList().replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        getList().sort(c);
    }

    @Override
    public void clear() {
        getList().clear();
    }

    @Override
    public boolean equals(Object o) {
        return getList().equals(o);
    }

    @Override
    public int hashCode() {
        return getList().hashCode();
    }

    @Override
    public E get(int index) {
        return getList().get(index);
    }

    @Override
    public E set(int index, E element) {
        return getList().set(index, element);
    }

    @Override
    public void add(int index, E element) {
        getList().add(index, element);
    }

    @Override
    public E remove(int index) {
        return getList().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return getList().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getList().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return getList().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return getList().listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return getList().subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<E> spliterator() {
        return getList().spliterator();
    }

    @Override
    public String toString() {
        return collection.toString();
    }
}
