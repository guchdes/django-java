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

import io.github.guchdes.django.bson.projection.containerlisten.ListenableMap;
import io.github.guchdes.django.bson.projection.pojo.ExternalMapStringKeyConverter;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 使用HashMap实现。
 * <p>
 * Map的key可以使用任意的Immutable类型，但是需要先注册该类型到String的转换方法。
 * {@link io.github.guchdes.django.bson.projection.pojo.GlobalModels#regMapStringKeyConverter(Class, ExternalMapStringKeyConverter)}
 * 默认可以使用String/Integer/Long/ObjectId.
 *
 * @Author guch
 * @Since 3.0.0
 */
@NotThreadSafe
public class DocumentMap<K, V> extends ContainerDocumentNode implements Map<K, V> {

    protected final Map<K, V> map;

    public DocumentMap() {
        this(new HashMap<>(), false);
    }

    public DocumentMap(Map<K, V> map) {
        this(map, false);
    }

    public DocumentMap(int initialCapacity, float loadFactor) {
        this(new HashMap<>(initialCapacity, loadFactor), false);
    }

    /**
     * @param isSyncRecording 是否对记录更新和修改map加锁，保证map的最终状态和更新记录一致。
     *                        此加锁不保证map本身的并发安全。(如不保证同时读写，同时写和遍历等操作并发安全)
     */
    protected DocumentMap(Map<K, V> map, boolean isSyncRecording) {
        this(node -> {
            return new ListenableMap<>(map, new ProxyMapListener<>(node), isSyncRecording);
        });
    }

    protected DocumentMap(Map<K, V> map, boolean isSyncRecording, boolean allowNullValues) {
        this(node -> {
            return new ListenableMap<>(map, new ProxyMapListener<>(node, allowNullValues), isSyncRecording);
        });
    }

    @SuppressWarnings("unchecked")
    protected DocumentMap(ListenableMapCreator creator) {
        this.map = (Map<K, V>) creator.get(this);
    }

    /**
     * 用来在多个构造方法或子类构造方法中获取父类引用
     */
    protected interface ListenableMapCreator {
        Map<?, ?> get(ContainerDocumentNode node);
    }

    protected DocumentMap(ListenableMap<K, V> listenableMap) {
        this.map = listenableMap;
    }

    @SuppressWarnings("unchecked")
    public HashMap<K, V> cloneToHashMap() {
        return (HashMap<K, V>) deepCloneMap(() -> new HashMap<>(size()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Map<K, V>> T cloneToMap(Supplier<T> mapSupplier) {
        return (T) deepCloneMap((Supplier) mapSupplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DocumentMap<K, V> deepCloneSelf() {
        return super.deepCloneSelf();
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
    @SuppressWarnings("rawtypes")
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
