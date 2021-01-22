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

import io.github.guchdes.django.bson.projection.containerlisten.ListenableLinkedList;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * 使用 {@link LinkedList}存储元素的DocumentList
 *
 * @author guch
 * @since 3.0.0
 */
@NotThreadSafe
public class LinkedDocumentList<E> extends DocumentList<E> implements Deque<E> {

    public LinkedDocumentList() {
        this(node -> {
            return new ListenableLinkedList<>(new LinkedList<>(), new ProxyListListener<>(node));
        });
    }

    public LinkedDocumentList(Collection<? extends E> c) {
        this(node -> {
            ListenableLinkedList<Object> list = new ListenableLinkedList<>(new LinkedList<>(c), new ProxyListListener<>(node));
            list.addAll(c);
            return list;
        });
    }

    protected LinkedDocumentList(ListenableCollectionCreator creator) {
        super(creator);
    }

    private ListenableLinkedList<E> getLinkedList() {
        return (ListenableLinkedList<E>) collection;
    }

    //deep clone to LinkedList
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends LinkedList<E>> T cloneToLinkedList() {
        return (T) deepCloneCollection((Supplier) LinkedList::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public LinkedDocumentList<E> deepCloneSelf() {
        return (LinkedDocumentList<E>) super.deepCloneSelf();
    }

    /* 代理方法 */

    @Override
    public void clear() {
        getLinkedList().clear();
    }

    @Override
    public E removeFirst() {
        return getLinkedList().removeFirst();
    }

    @Override
    public E removeLast() {
        return getLinkedList().removeLast();
    }

    @Override
    public void addFirst(E e) {
        getLinkedList().addFirst(e);
    }

    @Override
    public void addLast(E e) {
        getLinkedList().addLast(e);
    }

    @Override
    public boolean add(E e) {
        return getLinkedList().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return getLinkedList().remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return getLinkedList().addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return getLinkedList().addAll(index, c);
    }

    @Override
    public E set(int index, E element) {
        return getLinkedList().set(index, element);
    }

    @Override
    public void add(int index, E element) {
        getLinkedList().add(index, element);
    }

    @Override
    public E remove(int index) {
        return getLinkedList().remove(index);
    }

    @Override
    public E poll() {
        return getLinkedList().poll();
    }

    @Override
    public E remove() {
        return getLinkedList().remove();
    }

    @Override
    public boolean offer(E e) {
        return getLinkedList().offer(e);
    }

    @Override
    public boolean offerFirst(E e) {
        return getLinkedList().offerFirst(e);
    }

    @Override
    public boolean offerLast(E e) {
        return getLinkedList().offerLast(e);
    }

    @Override
    public E pollFirst() {
        return getLinkedList().pollFirst();
    }

    @Override
    public E pollLast() {
        return getLinkedList().pollLast();
    }

    @Override
    public void push(E e) {
        getLinkedList().push(e);
    }

    @Override
    public E pop() {
        return getLinkedList().pop();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return getLinkedList().removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return getLinkedList().removeLastOccurrence(o);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return getLinkedList().listIterator(index);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return getLinkedList().descendingIterator();
    }

    @Override
    public Iterator<E> iterator() {
        return getLinkedList().iterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return getLinkedList().listIterator();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return getLinkedList().subList(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return getLinkedList().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return getLinkedList().retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        getLinkedList().replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        getLinkedList().sort(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return getLinkedList().removeIf(filter);
    }

    @Override
    public E getFirst() {
        return getLinkedList().getFirst();
    }

    @Override
    public E getLast() {
        return getLinkedList().getLast();
    }

    @Override
    public boolean contains(Object o) {
        return getLinkedList().contains(o);
    }

    @Override
    public int size() {
        return getLinkedList().size();
    }

    @Override
    public E get(int index) {
        return getLinkedList().get(index);
    }

    @Override
    public int indexOf(Object o) {
        return getLinkedList().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getLinkedList().lastIndexOf(o);
    }

    @Override
    public E peek() {
        return getLinkedList().peek();
    }

    @Override
    public E element() {
        return getLinkedList().element();
    }

    @Override
    public E peekFirst() {
        return getLinkedList().peekFirst();
    }

    @Override
    public E peekLast() {
        return getLinkedList().peekLast();
    }

    @Override
    public Object[] toArray() {
        return getLinkedList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getLinkedList().toArray(a);
    }

    @Override
    public Spliterator<E> spliterator() {
        return getLinkedList().spliterator();
    }

    @Override
    public String toString() {
        return collection.toString();
    }
}
