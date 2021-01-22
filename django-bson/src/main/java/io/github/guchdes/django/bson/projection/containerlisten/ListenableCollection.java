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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.github.guchdes.django.bson.projection.containerlisten.RemoveAndGetObjectCollection.ResultState.REMOVE_FAILED;

/**
 * @author guch
 * @since 3.0.0
 */
public class ListenableCollection<E> implements Collection<E>, ListenableContainer {

    protected final Collection<E> collection;
    protected final CollectionListener<E> listener;
    protected final ChangeLock changeLock;
    protected final Object lock;
    protected final IncomingElementTransformer<E> transformer;

    public ListenableCollection(Collection<E> collection, CollectionListener<E> listener) {
        this(collection, listener, new ChangeLock(), null);
    }

    /**
     * @param isSync 是否对操作collection和调用listener加锁
     */
    public ListenableCollection(Collection<E> collection, CollectionListener<E> listener, boolean isSync) {
        this(collection, listener, new ChangeLock(), isSync ? new Object() : null);
    }

    ListenableCollection(Collection<E> collection, CollectionListener<E> listener, ChangeLock changeLock, Object lock) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(listener);
        this.collection = collection;
        this.listener = listener;
        this.transformer = listener.getIncomingElementTransformer();
        this.changeLock = changeLock;
        this.lock = lock;
    }

    protected <T> T doWithThreadLock(Supplier<T> action) {
        if (lock == null) {
            return action.get();
        } else {
            synchronized (lock) {
                return action.get();
            }
        }
    }

    protected void doWithThreadLock(Runnable action) {
        if (lock == null) {
            action.run();
        } else {
            synchronized (lock) {
                action.run();
            }
        }
    }

    protected E transform(E e, boolean hasLock) {
        if (!hasLock) {
            return e;
        }
        return transformer == null ? e : transformer.transform(e);
    }

    protected ListenableCollection<E> getThis() {
        return this;
    }

    @Override
    public ContainerListener<?, ?> getListener() {
        return listener;
    }

    @Override
    public String toString() {
        return collection.toString();
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return monitorIterator(collection.iterator());
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return collection.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(
                    (hasLock) -> {
                        E e1 = transform(e, hasLock);
                        boolean r = collection.add(e1);
                        if (hasLock && r) {
                            listener.afterAdd(getThis(), e1, true);
                        }
                        return r;
                    });
        });
    }

    @Override
    public boolean remove(Object o) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                Object r = removeInternal(o);
                if (r != REMOVE_FAILED && hasLock) {
                    listener.afterRemove(getThis(), r);
                }
                return r != REMOVE_FAILED;
            });
        });
    }

    /**
     * 参数和实际被移出的元素是equals关系，未必是同一个对象，但是要向listener传递实际被移出的对象，
     * 所以要遍历查找。List和Set可以重写此方法做优化。
     *
     * @return 返回实际被移除的元素，如果没有找到，返回 {@link RemoveAndGetObjectCollection.ResultState#REMOVE_FAILED}
     */
    protected Object removeInternal(Object o) {
        if (collection instanceof RemoveAndGetObjectCollection) {
            return ((RemoveAndGetObjectCollection) collection).removeAndGet(o);
        }

        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            E next = iterator.next();
            if (Objects.equals(next, o)) {
                iterator.remove();
                return next;
            }
        }
        return REMOVE_FAILED;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Objects.requireNonNull(c);
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock((hasLock) -> {
                Collection<? extends E> c1 = null;
                if (transformer != null && hasLock) {
                    List<E> list = new ArrayList<>(c.size());
                    for (E e : c) {
                        list.add(transformer.transform(e));
                    }
                    c1 = list;
                } else {
                    c1 = c;
                }
                boolean r = collection.addAll(c1);
                if (r && hasLock) {
                    listener.afterAddAll(getThis(), c1, true);
                }
                return r;
            });
        });
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock((hasLock) -> {
                boolean hasRemoved = false;
                List<Object> removed = hasLock ? new ArrayList<>(c.size()) : null;
                for (Object o : c) {
                    Object r = removeInternal(o);
                    if (r != REMOVE_FAILED) {
                        hasRemoved = true;
                        if (hasLock) {
                            removed.add(r);
                        }
                    }
                }
                if (hasLock && !removed.isEmpty()) {
                    listener.afterRemoveAll(getThis(), removed);
                }
                return hasRemoved;
            });
        });
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock((hasLock) -> {
                boolean hasRemoved = false;
                List<Object> removed = hasLock ? new ArrayList<>(c.size()) : null;
                Iterator<E> iterator = iterator();
                while (iterator.hasNext()) {
                    E next = iterator.next();
                    if (!c.contains(next)) {
                        iterator.remove();
                        if (hasLock) {
                            removed.add(next);
                        }
                        hasRemoved = true;
                    }
                }
                if (hasLock && hasRemoved) {
                    listener.afterRemoveAll(getThis(), removed);
                }
                return hasRemoved;
            });
        });
    }

    @Override
    public void clear() {
        doWithThreadLock(() -> {
            changeLock.doWithChangeLock((hasLock) -> {
                if (collection.isEmpty()) return false;
                List<E> removed = new ArrayList<>(this);
                collection.clear();
                if (hasLock) {
                    listener.afterClear(getThis(), removed);
                }
                return true;
            });
        });
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o.getClass() != this.getClass()) return false;
        return collection.equals(((ListenableCollection)o).collection);
    }

    @Override
    public int hashCode() {
        return collection.hashCode();
    }

    protected Iterator<E> monitorIterator(Iterator<E> iterator) {
        return new Iterator<E>() {
            private E lastRet;

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public E next() {
                return (lastRet = iterator.next());
            }

            public void remove() {
                doWithThreadLock(() -> {
                    changeLock.doWithChangeLock(() -> {
                        iterator.remove();
                        return true;
                    }, () -> listener.afterRemove(getThis(), lastRet));
                });
            }
        };
    }

    @Override
    public Spliterator<E> spliterator() {
        return collection.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return collection.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return collection.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        collection.forEach(action);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        List<Object> removed = new ArrayList<>();

        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(() -> {
                final Iterator<E> each = iterator();
                while (each.hasNext()) {
                    E next = each.next();
                    if (filter.test(next)) {
                        each.remove();
                        removed.add(next);
                    }
                }
                return !removed.isEmpty();
            }, () -> listener.afterRemoveAll(getThis(), removed));
        });
    }

    /**
     * 当创建一个当前容器的视图时, Listener中仍然要引用到当前容器, 而不是临时的视图容器, 所以要从此类创建
     * 视图容器的Listener. 此类可以访问到当前容器.
     */
    protected class ViewCollectionListener implements CollectionListener<E> {

        @Override
        public void afterRemove(Collection<E> ignored, Object value) {
            listener.afterRemove(getThis(), value);
        }

        @Override
        public void afterRemoveAll(Collection<E> ignored, Collection<?> values) {
            listener.afterRemoveAll(getThis(), values);
        }

        @Override
        public void afterAdd(Collection<E> ignored, E value, boolean isTail) {
            listener.afterAdd(getThis(), value, isTail);
        }

        @Override
        public void afterAddAll(Collection<E> ignored, Collection<? extends E> values, boolean isTail) {
            listener.afterAddAll(getThis(), values, isTail);
        }

        @Override
        public void afterClear(Collection<E> container, List<E> removed) {
            listener.afterClear(getThis(), removed);
        }

        @Nullable
        @Override
        public IncomingElementTransformer<E> getIncomingElementTransformer() {
            return listener.getIncomingElementTransformer();
        }
    }
}
