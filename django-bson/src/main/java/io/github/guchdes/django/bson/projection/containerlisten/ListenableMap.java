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

import io.github.guchdes.django.bson.projection.containerlisten.MapListener.IncomingMapValueTransformer;

import java.util.*;
import java.util.function.*;

/**
 * @author guch
 * @since 3.0.0
 */
public class ListenableMap<K, V> implements Map<K, V>, ListenableContainer {

    protected final Map<K, V> map;
    protected final MapListener<K, V> listener;
    protected final ChangeLock changeLock;
    protected final Object lock;
    protected final IncomingMapValueTransformer<K, V> transformer;

    public ListenableMap(Map<K, V> map, MapListener<K, V> listener) {
        this(map, listener, new ChangeLock(), null);
    }

    /**
     * @param isSync 是否对操作map和调用listener加锁
     */
    public ListenableMap(Map<K, V> map, MapListener<K, V> listener, boolean isSync) {
        this(map, listener, new ChangeLock(), isSync ? new Object() : null);
    }

    ListenableMap(Map<K, V> map, MapListener<K, V> listener, ChangeLock changeLock) {
        this(map, listener, changeLock, null);
    }

    ListenableMap(Map<K, V> map, MapListener<K, V> listener, ChangeLock changeLock, Object lock) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(listener);
        this.map = map;
        this.listener = listener;
        this.transformer = listener.getIncomingMapValueTransformer();
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

    protected ListenableMap<K, V> getThis() {
        return this;
    }

    protected V transform(K k, V e, boolean hasLock) {
        if (!hasLock) {
            return e;
        }
        return transformer == null ? e : transformer.transform(k, e);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock((hasLock) -> {
                V value1 = transform(key, value, hasLock);
                V pre = map.put(key, value1);
                if (hasLock) {
                    listener.afterPut(getThis(), key, value1, pre);
                }
                return pre;
            });
        });
    }

    @Override
    public V remove(Object key) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(() -> map.remove(key),
                    (r) -> listener.afterRemove(getThis(), key, r));
        });
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        doWithThreadLock(() -> {
            changeLock.doWithChangeLock((hasLock) -> {
                if (map.isEmpty()) {
                    return null;
                }
                if (hasLock) {
                    listener.beforeClear(getThis());
                }
                map.clear();
                return null;
            });
        });
    }

    @Override
    public Set<K> keySet() {
        return new BaseListenableViewFromEntrySet<>(map.entrySet(), Entry::getKey);
    }

    @Override
    public Collection<V> values() {
        return new BaseListenableViewFromEntrySet<>(map.entrySet(), Entry::getValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Entry<K, V>> entrySet() {
        return new BaseListenableViewFromEntrySet<>(map.entrySet(), ChangeListenerEntry::new);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }

            // ise thrown from function is not a cme.
            v = function.apply(k, v);

            try {
                // 在entry.setValue中调用过listener
                entry.setValue(v);
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                V value1 = transform(key, value, hasLock);
                V r = map.putIfAbsent(key, value1);
                //putIfAbsent返回null，则一定put了newValue
                if (hasLock && r == null) {
                    listener.afterPut(getThis(), key, value1, null);
                }
                return r;
            });
        });
    }

    @Override
    public boolean remove(Object key, Object value) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(() -> map.remove(key, value),
                    () -> listener.afterRemove(getThis(), key, value));
        });
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                V newValue1 = transform(key, newValue, hasLock);
                boolean replace = map.replace(key, oldValue, newValue1);
                if (hasLock && replace) {
                    listener.afterPut(getThis(), key, newValue1, oldValue);
                }
                return replace;
            });
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public V replace(K key, V value) {
        return doWithThreadLock(() -> {
            //无法根据map.replace的返回值判断是否修改了map，所以替换实现方式，不加changeLock，在put中调用listener
            V curValue;
            if (((curValue = get(key)) != null) || containsKey(key)) {
                curValue = put(key, value);
            }
            return curValue;
        });
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                boolean[] compute = new boolean[]{false};
                V v = map.computeIfAbsent(key, k -> {
                    compute[0] = true;
                    V apply = mappingFunction.apply(k);
                    return transform(key, apply, hasLock);
                });
                if (compute[0] && hasLock) {
                    if (v != null) {
                        listener.afterPut(getThis(), key, v, null);
                    }
                }
                return v;
            });
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                Object[] compute = new Object[]{false, null};
                V v = map.computeIfPresent(key, (k, oldV) -> {
                    compute[0] = true;
                    compute[1] = oldV;
                    V apply = remappingFunction.apply(k, oldV);
                    return transform(key, apply, hasLock);
                });
                if ((boolean) compute[0] && hasLock) {
                    if (v == null) {
                        listener.afterRemove(getThis(), key, compute[1]);
                    } else {
                        listener.afterPut(getThis(), key, v, (V) compute[1]);
                    }
                }
                return v;
            });
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                Object[] compute = new Object[]{null};
                V v = map.compute(key, (k, oldV) -> {
                    compute[0] = oldV;
                    V apply = remappingFunction.apply(k, oldV);
                    return transform(key, apply, hasLock);
                });
                if (hasLock) {
                    V oldV = (V) compute[0];
                    if (v != null) {
                        listener.afterPut(getThis(), key, v, oldV);
                    } else {
                        listener.afterRemove(getThis(), key, compute[0]);
                    }
                }
                return v;
            });
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock(hasLock -> {
                Object[] compute = new Object[]{null};
                V v = map.merge(key, value, (oldV, newV) -> {
                    compute[0] = oldV;
                    V apply = remappingFunction.apply(oldV, newV);
                    return transform(key, apply, hasLock);
                });
                if (hasLock) {
                    V oldV = (V) compute[0];
                    if (v != null) {
                        listener.afterPut(getThis(), key, v, oldV);
                    } else {
                        listener.afterRemove(getThis(), key, compute[0]);
                    }
                }
                return v;
            });
        });
    }

    /**
     * 使用 {@link AbstractMap#equals(java.lang.Object)}的实现代码
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        Map<?,?> m = (Map<?,?>) o;
        if (m.size() != size())
            return false;

        try {
            Iterator<Entry<K,V>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<K,V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key)==null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    @Override
    public ContainerListener<?, ?> getListener() {
        return listener;
    }

    /**
     * values()、keySet()、entrySet()的视图都从entrySet()创建，从而能在移除元素时知道对应的key和value
     */
    protected class BaseListenableViewFromEntrySet<T> extends AbstractSet<T> {
        final Set<Entry<K, V>> entrySet;

        final Function<Entry<K, V>, T> entryElementExtractor;

        public BaseListenableViewFromEntrySet(Set<Entry<K, V>> entrySet, Function<Entry<K, V>, T> entryElementExtractor) {
            this.entrySet = entrySet;
            this.entryElementExtractor = entryElementExtractor;
        }

        @Override
        public Iterator<T> iterator() {
            Iterator<Entry<K, V>> iterator = entrySet.iterator();
            return new Iterator<T>() {
                Entry<K, V> previous;

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public T next() {
                    Entry<K, V> entry = iterator.next();
                    previous = entry;
                    return entryElementExtractor.apply(entry);
                }

                @Override
                public void remove() {
                    Entry<K, V> previous = this.previous;
                    doWithThreadLock(() -> {
                        changeLock.doWithChangeLock(() -> {
                                    iterator.remove();
                                    return true;
                                },
                                () -> listener.afterRemove(getThis(), previous.getKey(), previous.getValue()));
                    });
                }
            };
        }

        @Override
        public int size() {
            return entrySet.size();
        }
    }

    protected class ChangeListenerEntry<K1, V1> implements Entry<K1, V1> {
        final Entry<K1, V1> entry;

        public ChangeListenerEntry(Entry<K1, V1> entry) {
            this.entry = entry;
        }

        @Override
        public K1 getKey() {
            return entry.getKey();
        }

        @Override
        public V1 getValue() {
            return entry.getValue();
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public V1 setValue(V1 value) {
            MapListener listener = ListenableMap.this.listener;
            return doWithThreadLock(() -> {
                return changeLock.doWithChangeLock((hasLock) -> {
                    V1 value1 = (V1) transform((K) entry.getKey(), (V) value, hasLock);
                    V1 r = entry.setValue(value1);
                    if (hasLock) {
                        listener.afterPut(getThis(), entry.getKey(), value1, r);
                    }
                    return r;
                });
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChangeListenerEntry<?, ?> that = (ChangeListenerEntry<?, ?>) o;
            return Objects.equals(entry, that.entry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entry);
        }
    }

}
