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

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class ListenableList<E> extends ListenableCollection<E> implements List<E> {

    public ListListener<E> getListener() {
        return (ListListener<E>) listener;
    }

    private List<E> getRawList() {
        return (List<E>) collection;
    }

    protected ListenableList<E> getThis() {
        return this;
    }

    public ListenableList(List<E> list, ListListener<E> listener) {
        super(list, listener);
    }

    /**
     * @param isSync 是否对操作collection和调用listener加锁
     */
    public ListenableList(List<E> list, ListListener<E> listener, boolean isSync) {
        super(list, listener, isSync);
    }

    ListenableList(List<E> list, ListListener<E> listener, ChangeLock changeLock, Object lock) {
        super(list, listener, changeLock, lock);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                boolean isTail = index == size();
                Collection<? extends E> c1 = (transformer != null && hasLock) ?
                        c.stream().map(transformer::transform).collect(Collectors.toList()) : c;
                boolean r = getRawList().addAll(index, c1);
                if (r && hasLock) {
                    listener.afterAddAll(getThis(), c1, isTail);
                }
                return r;
            });
        });
    }

    @Override
    public E get(int index) {
        return getRawList().get(index);
    }

    @Override
    public E set(int index, E element) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                E e1 = transform(element, hasLock);
                E pre = getRawList().set(index, e1);
                if (hasLock) {
                    getListener().afterSet(getThis(), index, e1, pre);
                }
                return pre;
            });
        });
    }

    @Override
    public void add(int index, E element) {
        doWithThreadLock(() -> {
            changeLock.doWithChangeLock((hasLock) -> {
                boolean isTail = index == size();
                E element1 = transform(element, hasLock);
                getRawList().add(index, element1);
                if (hasLock) {
                    listener.afterAdd(getThis(), element1, isTail);
                }
                return null;
            });
        });
    }

    @Override
    public E remove(int index) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(() -> getRawList().remove(index),
                    (r) -> getListener().afterRemove(getThis(), r));
        });
    }

    @Override
    public void sort(Comparator<? super E> c) {
        //对于sort只调用一次afterUnclassifiedChange
        doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(() -> {
                        getRawList().sort(c);
                        return null;
                    },
                    () -> getListener().afterStructChange(getThis()));
        });
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final ListIterator<E> li = this.listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }

    @Override
    public int indexOf(Object o) {
        return getRawList().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getRawList().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return monitorListIterator(getRawList().listIterator());
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return monitorListIterator(getRawList().listIterator(index));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<E> subList(int fromIndex, int toIndex) {
        List<E> subList = getRawList().subList(fromIndex, toIndex);
        return new ListenableList<>(subList, new ViewListListener(fromIndex, toIndex, subList), changeLock, lock);
    }

    /**
     * 使用 {@link AbstractList#equals(java.lang.Object)}的实现代码
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof List))
            return false;

        ListIterator<E> e1 = listIterator();
        ListIterator<?> e2 = ((List<?>) o).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    protected class ViewListListener extends ViewCollectionListener implements ListListener<E> {

        private final int fromIndex;

        private final int toIndex;

        private final List<E> subList;

        public ViewListListener(int fromIndex, int toIndex, List<E> subList) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.subList = subList;
        }

        @Override
        public void afterSet(List<E> list, int index, E e, E pre) {
            getListener().afterSet(getThis(), index + fromIndex, e, pre);
        }

        @Override
        public void afterStructChange(List<E> list) {
            getListener().afterStructChange(getThis());
        }

        @Override
        public void afterAdd(Collection<E> ignored, E value, boolean isTail) {
            super.afterAdd(ignored, value, false);
        }

        @Override
        public void afterAddAll(Collection<E> ignored, Collection<? extends E> values, boolean isTail) {
            super.afterAddAll(ignored, values, false);
        }

        @Override
        public void afterClear(Collection<E> container, List<E> removed) {
            super.afterRemoveAll(getThis(), removed);
        }

    }

    private ListIterator<E> monitorListIterator(ListIterator<E> iterator) {
        return new ListIterator<E>() {
            E lastRet;
            int lastIndex;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                E r;
                r = lastRet = iterator.next();
                lastIndex = iterator.nextIndex() - 1;
                return r;
            }

            @Override
            public boolean hasPrevious() {
                return iterator.hasPrevious();
            }

            @Override
            public E previous() {
                E r;
                r = lastRet = iterator.previous();
                lastIndex = iterator.previousIndex() + 1;
                return r;
            }

            @Override
            public int nextIndex() {
                return iterator.nextIndex();
            }

            @Override
            public int previousIndex() {
                return iterator.previousIndex();
            }

            @Override
            public void remove() {
                doWithThreadLock(() -> {
                    changeLock.doWithChangeLock(() -> {
                        iterator.remove();
                        return true;
                    }, () -> listener.afterRemove(getThis(), lastRet));
                });
            }

            @Override
            public void set(E e) {
                doWithThreadLock(() -> {
                    changeLock.doWithChangeLock((hasLock) -> {
                        E e1 = transform(e, hasLock);
                        iterator.set(e1);
                        if (hasLock) {
                            getListener().afterSet(getThis(), lastIndex, e1, lastRet);
                        }
                        return true;
                    });
                });
            }

            @Override
            public void add(E e) {
                doWithThreadLock(() -> {
                    changeLock.doWithChangeLock((hasLock) -> {
                        E e1 = transform(e, hasLock);
                        iterator.add(e1);
                        if (hasLock) {
                            listener.afterAdd(getThis(), e1, false);
                        }
                        return true;
                    });
                });
            }
        };
    }

}
