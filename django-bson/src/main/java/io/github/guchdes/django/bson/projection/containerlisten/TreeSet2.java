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

import static io.github.guchdes.django.bson.projection.containerlisten.RemoveAndGetObjectCollection.ResultState.REMOVE_FAILED;

/**
 * JDK1.8中的TreeSet，为了实现 {@link RemoveAndGetObjectCollection} 做了一点修改
 */
public class TreeSet2<E> extends AbstractSet<E>
        implements NavigableSet<E>, Cloneable, java.io.Serializable, RemoveAndGetObjectCollection {
    /**
     * The backing map.
     */
    private transient NavigableMap<E, Object> m;

    /**
     * map的Entry中key和value是同一个对象，但key为null时除外，key为null时value使用此字段
     */
    private static final Object NULL = new Object();

    /**
     * Constructs a set backed by the specified navigable map.
     */
    TreeSet2(NavigableMap<E, Object> m) {
        this.m = m;
    }

    /**
     * Constructs a new, empty tree set, sorted according to the
     * natural ordering of its elements.  All elements inserted into
     * the set must implement the {@link Comparable} interface.
     * Furthermore, all such elements must be <i>mutually
     * comparable</i>: {@code e1.compareTo(e2)} must not throw a
     * {@code ClassCastException} for any elements {@code e1} and
     * {@code e2} in the set.  If the user attempts to add an element
     * to the set that violates this constraint (for example, the user
     * attempts to add a string element to a set whose elements are
     * integers), the {@code add} call will throw a
     * {@code ClassCastException}.
     */
    public TreeSet2() {
        this(new TreeMap<E, Object>());
    }

    /**
     * Constructs a new, empty tree set, sorted according to the specified
     * comparator.  All elements inserted into the set must be <i>mutually
     * comparable</i> by the specified comparator: {@code comparator.compare(e1,
     * e2)} must not throw a {@code ClassCastException} for any elements
     * {@code e1} and {@code e2} in the set.  If the user attempts to add
     * an element to the set that violates this constraint, the
     * {@code add} call will throw a {@code ClassCastException}.
     *
     * @param comparator the comparator that will be used to order this set.
     *                   If {@code null}, the {@linkplain Comparable natural
     *                   ordering} of the elements will be used.
     */
    public TreeSet2(Comparator<? super E> comparator) {
        this(new TreeMap<>(comparator));
    }

    /**
     * Constructs a new tree set containing the elements in the specified
     * collection, sorted according to the <i>natural ordering</i> of its
     * elements.  All elements inserted into the set must implement the
     * {@link Comparable} interface.  Furthermore, all such elements must be
     * <i>mutually comparable</i>: {@code e1.compareTo(e2)} must not throw a
     * {@code ClassCastException} for any elements {@code e1} and
     * {@code e2} in the set.
     *
     * @param c collection whose elements will comprise the new set
     * @throws ClassCastException   if the elements in {@code c} are
     *                              not {@link Comparable}, or are not mutually comparable
     * @throws NullPointerException if the specified collection is null
     */
    public TreeSet2(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * Constructs a new tree set containing the same elements and
     * using the same ordering as the specified sorted set.
     *
     * @param s sorted set whose elements will comprise the new set
     * @throws NullPointerException if the specified sorted set is null
     */
    public TreeSet2(SortedSet<E> s) {
        this(s.comparator());
        addAll(s);
    }

    /**
     * Returns an iterator over the elements in this set in ascending order.
     *
     * @return an iterator over the elements in this set in ascending order
     */
    public Iterator<E> iterator() {
        return m.navigableKeySet().iterator();
    }

    /**
     * Returns an iterator over the elements in this set in descending order.
     *
     * @return an iterator over the elements in this set in descending order
     * @since 1.6
     */
    public Iterator<E> descendingIterator() {
        return m.descendingKeySet().iterator();
    }

    /**
     * @since 1.6
     */
    public NavigableSet<E> descendingSet() {
        return new TreeSet2<>(m.descendingMap());
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality)
     */
    public int size() {
        return m.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements
     */
    public boolean isEmpty() {
        return m.isEmpty();
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     * More formally, returns {@code true} if and only if this set
     * contains an element {@code e} such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o object to be checked for containment in this set
     * @return {@code true} if this set contains the specified element
     * @throws ClassCastException   if the specified object cannot be compared
     *                              with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *                              and this set uses natural ordering, or its comparator
     *                              does not permit null elements
     */
    public boolean contains(Object o) {
        return m.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element {@code e} to this set if
     * the set contains no element {@code e2} such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns {@code false}.
     *
     * @param e element to be added to this set
     * @return {@code true} if this set did not already contain the specified
     * element
     * @throws ClassCastException   if the specified object cannot be compared
     *                              with the elements currently in this set
     * @throws NullPointerException if the specified element is null
     *                              and this set uses natural ordering, or its comparator
     *                              does not permit null elements
     */
    public boolean add(E e) {
        return m.put(e, e == null ? NULL : e) == null;
    }

    /**
     * Removes the specified element from this set if it is present.
     * More formally, removes an element {@code e} such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>,
     * if this set contains such an element.  Returns {@code true} if
     * this set contained the element (or equivalently, if this set
     * changed as a result of the call).  (This set will not contain the
     * element once the call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if this set contained the specified element
     * @throws ClassCastException   if the specified object cannot be compared
     *                              with the elements currently in this set
     * @throws NullPointerException if the specified element is null
     *                              and this set uses natural ordering, or its comparator
     *                              does not permit null elements
     */
    public boolean remove(Object o) {
        return m.remove(o) != null;
    }

    /**
     * Removes all of the elements from this set.
     * The set will be empty after this call returns.
     */
    public void clear() {
        m.clear();
    }

    @Override
    public Object removeAndGet(Object o) {
        Object r = m.remove(o);
        if (r == null) {
            return REMOVE_FAILED;
        } else {
            return r == NULL ? null : r;
        }
    }

    /**
     * subSet仍然是用自身实现，所以也支持 {@link RemoveAndGetObjectCollection}
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     if {@code fromElement} or {@code toElement}
     *                                  is null and this set uses natural ordering, or its comparator
     *                                  does not permit null elements
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.6
     */
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                                  E toElement, boolean toInclusive) {
        return new TreeSet2<>(m.subMap(fromElement, fromInclusive,
                toElement, toInclusive));
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     if {@code toElement} is null and
     *                                  this set uses natural ordering, or its comparator does
     *                                  not permit null elements
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.6
     */
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new TreeSet2<>(m.headMap(toElement, inclusive));
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     if {@code fromElement} is null and
     *                                  this set uses natural ordering, or its comparator does
     *                                  not permit null elements
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.6
     */
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new TreeSet2<>(m.tailMap(fromElement, inclusive));
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     if {@code fromElement} or
     *                                  {@code toElement} is null and this set uses natural ordering,
     *                                  or its comparator does not permit null elements
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     if {@code toElement} is null
     *                                  and this set uses natural ordering, or its comparator does
     *                                  not permit null elements
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     if {@code fromElement} is null
     *                                  and this set uses natural ordering, or its comparator does
     *                                  not permit null elements
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    public Comparator<? super E> comparator() {
        return m.comparator();
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E first() {
        return m.firstKey();
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public E last() {
        return m.lastKey();
    }

    // NavigableSet API methods

    /**
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException if the specified element is null
     *                              and this set uses natural ordering, or its comparator
     *                              does not permit null elements
     * @since 1.6
     */
    public E lower(E e) {
        return m.lowerKey(e);
    }

    /**
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException if the specified element is null
     *                              and this set uses natural ordering, or its comparator
     *                              does not permit null elements
     * @since 1.6
     */
    public E floor(E e) {
        return m.floorKey(e);
    }

    /**
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException if the specified element is null
     *                              and this set uses natural ordering, or its comparator
     *                              does not permit null elements
     * @since 1.6
     */
    public E ceiling(E e) {
        return m.ceilingKey(e);
    }

    /**
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException if the specified element is null
     *                              and this set uses natural ordering, or its comparator
     *                              does not permit null elements
     * @since 1.6
     */
    public E higher(E e) {
        return m.higherKey(e);
    }

    /**
     * @since 1.6
     */
    public E pollFirst() {
        Map.Entry<E, ?> e = m.pollFirstEntry();
        return (e == null) ? null : e.getKey();
    }

    /**
     * @since 1.6
     */
    public E pollLast() {
        Map.Entry<E, ?> e = m.pollLastEntry();
        return (e == null) ? null : e.getKey();
    }

    /**
     * Returns a shallow copy of this {@code TreeSet} instance. (The elements
     * themselves are not cloned.)
     *
     * @return a shallow copy of this set
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        TreeSet2<E> clone;
        try {
            clone = (TreeSet2<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }

        clone.m = new TreeMap<>(m);
        return clone;
    }

    /**
     * Save the state of the {@code TreeSet} instance to a stream (that is,
     * serialize it).
     *
     * @serialData Emits the comparator used to order this set, or
     * {@code null} if it obeys its elements' natural ordering
     * (Object), followed by the size of the set (the number of
     * elements it contains) (int), followed by all of its
     * elements (each an Object) in order (as determined by the
     * set's Comparator, or by the elements' natural ordering if
     * the set has no Comparator).
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // Write out any hidden stuff
        s.defaultWriteObject();

        // Write out Comparator
        s.writeObject(m.comparator());

        // Write out size
        s.writeInt(m.size());

        // Write out all elements in the proper order.
        for (E e : m.keySet())
            s.writeObject(e);
    }

    /**
     * Reconstitute the {@code TreeSet} instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden stuff
        s.defaultReadObject();

        // Read in Comparator
        @SuppressWarnings("unchecked")
        Comparator<? super E> c = (Comparator<? super E>) s.readObject();

        // Create backing TreeMap
        TreeMap<E, Object> tm = new TreeMap<>(c);
        m = tm;

        // Read in size
        int size = s.readInt();

        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked")
            E e = (E) s.readObject();
            tm.put(e, e == null ? NULL : e);
        }
    }

    private static final long serialVersionUID = -2479143000061671589L;
}
