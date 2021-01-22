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

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 有序的DocumentMap.
 *
 * @author guch
 * @since 3.0.0
 */
@NotThreadSafe
public class LinkedDocumentMap<K, V> extends DocumentMap<K, V> implements Map<K, V> {

    public LinkedDocumentMap() {
        super(new LinkedHashMap<>(), false);
    }

    public LinkedDocumentMap(Map<? extends K, ? extends V> map) {
        super(new LinkedHashMap<>(map), false);
    }

    protected LinkedDocumentMap(boolean allowNullValues) {
        super(new LinkedHashMap<>(), false, allowNullValues);
    }

    protected LinkedDocumentMap(Map<? extends K, ? extends V> map, boolean allowNullValues) {
        super(new LinkedHashMap<>(map), false, allowNullValues);
    }

    @SuppressWarnings("unchecked")
    public LinkedHashMap<K, V> cloneToLinkedHashMap() {
        return (LinkedHashMap<K, V>) deepCloneMap(() -> new LinkedHashMap<>(size()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public LinkedDocumentMap<K, V> deepCloneSelf() {
        return (LinkedDocumentMap<K, V>) super.deepCloneSelf();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
