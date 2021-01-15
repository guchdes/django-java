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

import io.github.guchdes.django.bson.projection.containerlisten.CollectionListener.IncomingElementTransformer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static io.github.guchdes.django.bson.projection.containerlisten.RemoveAndGetObjectCollection.ResultState.REMOVE_FAILED;

/**
 * 监听ListenableLinkedList.
 *
 * @Author guch
 * @Since 3.0.0
 */
public class ListenableLinkedList<E> extends ListenableList<E> implements ListenableContainer, Deque<E> {

    public ListenableLinkedList(LinkedList<E> linkedList, ListListener<E> listListener) {
        super(linkedList, listListener);
    }

    protected ListenableLinkedList<E> getThis() {
        return this;
    }

    private LinkedList<E> getLinkedList() {
        return (LinkedList<E>) collection;
    }

    @Override
    public E removeFirst() {
        return changeLock.doWithChangeLock(hasLock -> {
            E r = getLinkedList().removeFirst();
            if (hasLock) {
                getListener().afterRemove(getThis(), r);
            }
            return r;
        });
    }

    @Override
    public E removeLast() {
        return changeLock.doWithChangeLock(hasLock -> {
            E r = getLinkedList().removeLast();
            if (hasLock) {
                getListener().afterRemove(getThis(), r);
            }
            return r;
        });
    }

    @Override
    public void addFirst(E e) {
        changeLock.doWithChangeLock(hasLock -> {
            E e1 = transform(e, hasLock);
            getLinkedList().addFirst(e1);
            if (hasLock) {
                getListener().afterAdd(getThis(), e1, false);
            }
            return null;
        });
    }

    @Override
    public void addLast(E e) {
        changeLock.doWithChangeLock(hasLock -> {
            E e1 = transform(e, hasLock);
            getLinkedList().addLast(e1);
            if (hasLock) {
                getListener().afterAdd(getThis(), e1, true);
            }
            return null;
        });
    }

    @Override
    public E poll() {
        return changeLock.doWithChangeLock((hasLock) -> {
            boolean isEmpty = isEmpty();
            E r = getLinkedList().poll();
            //不能通过r==null判断是否移除了元素
            if (hasLock && !isEmpty) {
                listener.afterRemove(getThis(), r);
            }
            return r;
        });
    }

    @Override
    public E element() {
        return getLinkedList().getFirst();
    }

    @Override
    public E peek() {
        return getLinkedList().peek();
    }

    @Override
    public boolean offer(E e) {
        return this.add(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public boolean offerFirst(E e) {
        this.addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        this.addLast(e);
        return true;
    }

    @Override
    public E pollFirst() {
        return changeLock.doWithChangeLock((hasLock) -> {
            boolean isEmpty = isEmpty();
            E r = getLinkedList().pollFirst();
            //不能通过r==null判断是否移除了元素
            if (hasLock && !isEmpty) {
                listener.afterRemove(getThis(), r);
            }
            return r;
        });
    }

    @Override
    public E pollLast() {
        return changeLock.doWithChangeLock((hasLock) -> {
            boolean isEmpty = isEmpty();
            E r = getLinkedList().pollLast();
            //不能通过r==null判断是否移除了元素
            if (hasLock && !isEmpty) {
                listener.afterRemove(getThis(), r);
            }
            return r;
        });
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
    public E peekFirst() {
        return getLinkedList().peekFirst();
    }

    @Override
    public E peekLast() {
        return getLinkedList().peekLast();
    }

    @Override
    public void push(E e) {
        this.addFirst(e);
    }

    @Override
    public E pop() {
        return this.removeFirst();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return monitorIterator(getLinkedList().descendingIterator());
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return this.remove(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return changeLock.doWithChangeLock(hasLock -> {
            Iterator<E> iterator = getLinkedList().descendingIterator();
            while (iterator.hasNext()) {
                E next = iterator.next();
                if (Objects.equals(next, o)) {
                    iterator.remove();
                    if (hasLock) {
                        listener.afterRemove(getThis(), next);
                    }
                    return true;
                }
            }
            return false;
        });
    }

}
