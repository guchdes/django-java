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

import javax.annotation.concurrent.NotThreadSafe;
import java.util.EnumMap;
import java.util.Map;

/**
 * Map的实现是EnumMap。入库时属性名默认为Enum名，如果Enum类实现了 {@link IndexedEnum}，则属性名是index。
 *
 * @Author guch
 * @Since 3.0.0
 */
@NotThreadSafe
public class DocumentEnumMap<K extends Enum<K>, V> extends DocumentMap<K, V> implements Map<K, V> {

    private final Class<K> keyType;

    public DocumentEnumMap(Class<K> keyType) {
        this(node -> {
            return new ListenableMap<>(new EnumMap<>(keyType), new ProxyMapListener<>(node), false);
        }, keyType);
    }

    public DocumentEnumMap(Map<K, V> map, Class<K> keyType) {
        this(node -> {
            return new ListenableMap<>(new EnumMap<>(map), new ProxyMapListener<>(node), false);
        }, keyType);
    }

    @SuppressWarnings("unchecked")
    private DocumentEnumMap(ListenableMapCreator creator, Class<K> keyType) {
        super(creator);
        this.keyType = keyType;
    }

    @SuppressWarnings("unchecked")
    public EnumMap<K, V> cloneToEnumMap() {
        return (EnumMap<K, V>) (Object) deepCloneMap(() -> new EnumMap<>(keyType));
    }

    @Override
    @SuppressWarnings("unchecked")
    public DocumentEnumMap<K, V> deepCloneSelf() {
        return (DocumentEnumMap<K, V>) super.deepCloneSelf();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
