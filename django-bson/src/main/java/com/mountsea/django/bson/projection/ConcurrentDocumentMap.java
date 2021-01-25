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

import com.mountsea.django.bson.projection.containerlisten.ListenableMap;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 使用ConcurrentHashMap实现。
 * get等读取方法是无锁的。
 *
 * @author guch
 * @since 3.0.0
 */
@ThreadSafe
public class ConcurrentDocumentMap<K, V> extends DocumentMap<K, V> implements ConcurrentMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;

    private static final float LOAD_FACTOR = 0.75f;

    protected final Map<K, V> map;

    public ConcurrentDocumentMap() {
        this(node -> {
            return new ListenableMap<>(new ConcurrentHashMap<>(DEFAULT_CAPACITY, LOAD_FACTOR, 1),
                    new ProxyMapListener<>(node), true);
        });
    }

    public ConcurrentDocumentMap(int initialCapacity, float loadFactor) {
        this(node -> {
            return new ListenableMap<>(new ConcurrentHashMap<>(initialCapacity, loadFactor),
                    new ProxyMapListener<>(node), true);
        });
    }

    @SuppressWarnings("unchecked")
    private ConcurrentDocumentMap(ListenableMapCreator creator) {
        this.map = (Map<K, V>) creator.get(this);
    }

    /**
     * 用来在多个构造方法或子类构造方法中获取父类引用
     */
    protected interface ListenableMapCreator {
        Map<?, ?> get(ContainerDocumentNode node);
    }

    @SuppressWarnings("unchecked")
    public HashMap<K, V> toHashMap() {
        return (HashMap<K, V>) deepCloneMap(() -> new HashMap<>(size()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Map<K, V>> T toMap(Supplier<T> mapSupplier) {
        return (T) deepCloneMap((Supplier<Map<?, ?>>) mapSupplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConcurrentDocumentMap<K, V> deepCloneSelf() {
        return (ConcurrentDocumentMap<K, V>) super.deepCloneSelf();
    }

    /* 以下是对map字段所有方法的代理 */

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
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return map.equals(o);
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
        map.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return map.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return map.merge(key, value, remappingFunction);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
