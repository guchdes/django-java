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

/**
 * @Author guch
 * @Since 3.0.0
 */
public class ListenableNavigableMap<K, V> extends ListenableSortedMap<K, V> implements NavigableMap<K, V> {

    public ListenableNavigableMap(NavigableMap<K, V> map, MapListener<K, V> listener) {
        super(map, listener);
    }

    /**
     * @param isSync 是否对操作collection和调用listener加锁
     */
    public ListenableNavigableMap(NavigableMap<K, V> map, MapListener<K, V> listener, boolean isSync) {
        super(map, listener, isSync);
    }

    ListenableNavigableMap(NavigableMap<K, V> map, MapListener<K, V> listener, ChangeLock changeLock, Object lock) {
        super(map, listener, changeLock, lock);
    }

    private NavigableMap<K, V> getMap() {
        return (NavigableMap<K, V>) map;
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return getMap().lowerEntry(key);
    }

    @Override
    public K lowerKey(K key) {
        return getMap().lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return new ChangeListenerEntry<>(getMap().floorEntry(key));
    }

    @Override
    public K floorKey(K key) {
        return getMap().floorKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return new ChangeListenerEntry<>(getMap().ceilingEntry(key));
    }

    @Override
    public K ceilingKey(K key) {
        return getMap().ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return new ChangeListenerEntry<>(getMap().higherEntry(key));
    }

    @Override
    public K higherKey(K key) {
        return getMap().higherKey(key);
    }

    @Override
    public Entry<K, V> firstEntry() {
        return wrapEntry(getMap().firstEntry());
    }

    @Override
    public Entry<K, V> lastEntry() {
        return wrapEntry(getMap().lastEntry());
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return doWithThreadLock(() -> {
            boolean tryLock = changeLock.tryLock();
            Entry<K, V> r;
            try {
                r = getMap().pollFirstEntry();
                if (tryLock && r != null) {
                    listener.afterRemove(this, r.getKey(), r.getValue());
                }
            } finally {
                if (tryLock) {
                    changeLock.tryUnlock();
                }
            }
            // poll之后的entry不能setValue
            return r;
        });
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return doWithThreadLock(() -> {
            boolean tryLock = changeLock.tryLock();
            Entry<K, V> r;
            try {
                r = getMap().pollLastEntry();
                if (tryLock && r != null) {
                    listener.afterRemove(this, r.getKey(), r.getValue());
                }
            } finally {
                if (tryLock) {
                    changeLock.tryUnlock();
                }
            }
            // poll之后的entry不能setValue
            return r;
        });
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return new ListenableNavigableMap<>(getMap().descendingMap(), new ViewMapListener(), changeLock, lock);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return new NavigableKeySet(this);
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return new NavigableKeySet(this.descendingMap());
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return new ListenableNavigableMap<>(getMap().subMap(fromKey, fromInclusive, toKey, toInclusive),
                new ViewMapListener(), changeLock, lock);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return new ListenableNavigableMap<>(getMap().headMap(toKey, inclusive),
                new ViewMapListener(), changeLock, lock);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return new ListenableNavigableMap<>(getMap().tailMap(fromKey, inclusive),
                new ViewMapListener(), changeLock, lock);
    }

    private <KK, VV> Entry<KK, VV> wrapEntry(Entry<KK, VV> entry) {
        if (entry == null) return null;
        if (entry instanceof AbstractMap.SimpleImmutableEntry) {
            return entry;  // 不能setValue
        } else {
            return new ChangeListenerEntry<>(entry); // 有可能setValue
        }
    }

    class NavigableKeySet extends AbstractSet<K> implements NavigableSet<K> {
        private final NavigableMap<K, ?> m;
        NavigableKeySet(NavigableMap<K,?> map) { m = map; }

        class KeyIterator implements Iterator<K> {
            final Iterator<? extends Entry<K, ?>> iterator;

            Entry<K, ?> lastEntry;

            KeyIterator(Iterator<? extends Entry<K, ?>> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public K next() {
                Entry<K, ?> entry = iterator.next();
                lastEntry = entry;
                return entry.getKey();
            }

            @Override
            public void remove() {
                Entry<K, ?> previous = this.lastEntry;
                doWithThreadLock(() -> {
                    changeLock.doWithChangeLock(() -> {
                                iterator.remove();
                                return true;
                            },
                            () -> listener.afterRemove(getThis(), previous.getKey(), previous.getValue()));
                });
            }
        }
        public Iterator<K> iterator() {
            return new KeyIterator(m.entrySet().iterator());
        }

        public Iterator<K> descendingIterator() {
            return new KeyIterator(m.descendingMap().entrySet().iterator());
        }

        public int size() { return m.size(); }
        public boolean isEmpty() { return m.isEmpty(); }
        public boolean contains(Object o) { return m.containsKey(o); }
        public void clear() { m.clear(); }
        public K lower(K e) { return m.lowerKey(e); }
        public K floor(K e) { return m.floorKey(e); }
        public K ceiling(K e) { return m.ceilingKey(e); }
        public K higher(K e) { return m.higherKey(e); }
        public K first() { return m.firstKey(); }
        public K last() { return m.lastKey(); }
        public Comparator<? super K> comparator() { return m.comparator(); }
        public K pollFirst() {
            Map.Entry<K,?> e = m.pollFirstEntry();
            return (e == null) ? null : e.getKey();
        }
        public K pollLast() {
            Map.Entry<K,?> e = m.pollLastEntry();
            return (e == null) ? null : e.getKey();
        }
        public boolean remove(Object o) {
            int oldSize = size();
            m.remove(o);
            return size() != oldSize;
        }
        public NavigableSet<K> subSet(K fromElement, boolean fromInclusive,
                                      K toElement, boolean toInclusive) {
            return new NavigableKeySet(m.subMap(fromElement, fromInclusive,
                    toElement,   toInclusive));
        }
        public NavigableSet<K> headSet(K toElement, boolean inclusive) {
            return new NavigableKeySet(m.headMap(toElement, inclusive));
        }
        public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
            return new NavigableKeySet(m.tailMap(fromElement, inclusive));
        }
        public SortedSet<K> subSet(K fromElement, K toElement) {
            return subSet(fromElement, true, toElement, false);
        }
        public SortedSet<K> headSet(K toElement) {
            return headSet(toElement, false);
        }
        public SortedSet<K> tailSet(K fromElement) {
            return tailSet(fromElement, true);
        }
        public NavigableSet<K> descendingSet() {
            return new NavigableKeySet(m.descendingMap());
        }
    }

    protected class NavigableKeySetFromEntrySet extends AbstractSet<K> implements NavigableSet<K> {



        @Override
        public Iterator<K> iterator() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public K lower(K k) {
            return null;
        }

        @Override
        public K floor(K k) {
            return null;
        }

        @Override
        public K ceiling(K k) {
            return null;
        }

        @Override
        public K higher(K k) {
            return null;
        }

        @Override
        public K pollFirst() {
            return null;
        }

        @Override
        public K pollLast() {
            return null;
        }

        @Override
        public NavigableSet<K> descendingSet() {
            return null;
        }

        @Override
        public Iterator<K> descendingIterator() {
            return null;
        }

        @Override
        public NavigableSet<K> subSet(K fromElement, boolean fromInclusive, K toElement, boolean toInclusive) {
            return null;
        }

        @Override
        public NavigableSet<K> headSet(K toElement, boolean inclusive) {
            return null;
        }

        @Override
        public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
            return null;
        }

        @Override
        public SortedSet<K> subSet(K fromElement, K toElement) {
            return null;
        }

        @Override
        public SortedSet<K> headSet(K toElement) {
            return null;
        }

        @Override
        public SortedSet<K> tailSet(K fromElement) {
            return null;
        }

        @Override
        public Comparator<? super K> comparator() {
            return null;
        }

        @Override
        public K first() {
            return null;
        }

        @Override
        public K last() {
            return null;
        }
    }
}
