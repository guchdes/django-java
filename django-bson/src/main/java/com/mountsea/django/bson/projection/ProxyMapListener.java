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

import com.mountsea.django.bson.projection.containerlisten.MapListener;
import com.mountsea.django.bson.projection.pojo.ExternalMapStringKeyConverter;
import com.mountsea.django.bson.projection.pojo.GlobalModels;
import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author guch
 * @since 3.0.0
 */
class ProxyMapListener<K, V> extends ProxyContainerListener<V, Map<K, V>> implements MapListener<K, V> {

    private final IncomingMapValueTransformer<K, V> transformer;

    public ProxyMapListener(ContainerDocumentNode node) {
        this(node, true);
    }

    public ProxyMapListener(ContainerDocumentNode node, boolean allowNullValues) {
        super(node);
        transformer = allowNullValues ? null : (k, v) -> {
            if (v == null) {
                throw new IllegalArgumentException("Not allow null values in container :" + node.getClass());
            }
            return v;
        };
    }

    @AllArgsConstructor
    private static class KeyClassAndConverter<K> {
        private final Class<K> keyClass;
        private final ExternalMapStringKeyConverter<K> converter;
    }

    private volatile KeyClassAndConverter<K> keyClassAndConverter;

    @SuppressWarnings("unchecked")
    protected String keyToString(K k) {
        KeyClassAndConverter<K> keyClassAndConverter = this.keyClassAndConverter;
        if (keyClassAndConverter == null || !keyClassAndConverter.keyClass.equals(k.getClass())) {
            ExternalMapStringKeyConverter<K> converter = (ExternalMapStringKeyConverter<K>)
                    GlobalModels.getOrCreateStringKeyConverter(k.getClass());
            this.keyClassAndConverter = keyClassAndConverter = new KeyClassAndConverter<>((Class<K>) k.getClass(), converter);
        }
        return keyClassAndConverter.converter.toString(k);
    }

    @Nullable
    @Override
    public IncomingMapValueTransformer<K, V> getIncomingMapValueTransformer() {
        return transformer;
    }

    @Override
    public void afterPut(Map<K, V> map, K k, V v, V previous) {
        processLeaveElement(previous);
        processIncomingElement(k, v);
        node.recordFieldAssign(v, previous, keyToString(k));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterRemove(Map<K, V> map, Object k, Object value) {
        processLeaveElement(value);
        node.recordFieldAssign(null, value, keyToString((K) k));
    }

    @Override
    public void beforeClear(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            processLeaveElement(entry.getValue());
        }
        node.recordSelfAssign();
    }

    protected void processIncomingElement(K key, V v) {
        if (v instanceof DocumentNode) {
            ((DocumentNode) v).setParent(node, keyToString(key));
        }
    }

    protected void processLeaveElement(Object e) {
        if (e instanceof DocumentNode) {
            ((DocumentNode) e).unsetParent(node);
        }
    }

}
