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

import io.github.guchdes.django.bson.KnownTypes;
import io.github.guchdes.django.bson.projection.containerlisten.SetListener;
import io.github.guchdes.django.bson.projection.pojo.GlobalModels;
import org.apache.commons.lang3.ClassUtils;
import org.bson.codecs.Codec;
import org.bson.codecs.ValueCodecProvider;

import java.util.Collection;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class ProxySetListener<E> extends ProxyCollectionListener<E> implements SetListener<E> {

    public ProxySetListener(ContainerDocumentNode node) {
        super(node);
    }

    @Override
    public void afterAdd(Collection<E> collection, E value, boolean isTail) {
        processIncomingElement(value);
        node.recordCollectionOp((collector, path) -> {
            collector.addToSetArrayValue(path, collection, value);
        });
    }

    @Override
    public void afterAddAll(Collection<E> collection, Collection<? extends E> values, boolean isTail) {
        for (E value : values) {
            processIncomingElement(value);
        }
        node.recordCollectionOp((collector, path) -> {
            collector.addToSetArrayValueBatch(path, collection, values);
        });
    }

    @Override
    public void afterRemove(Collection<E> collection, Object value) {
        processLeaveElement(value);
        if (fastDetermineImmutable(value)) {
            node.recordCollectionOp((collector, path) -> {
                collector.pullArrayValue(path, collection, value);
            });
        } else {
            recordSelfAssign();
        }
    }

    @Override
    public void afterRemoveAll(Collection<E> collection, Collection<?> values) {
        boolean hasMutable = false;
        for (Object value : values) {
            if (!hasMutable && !fastDetermineImmutable(value)) {
                hasMutable = true;
            }
            processLeaveElement(value);
        }
        if (hasMutable) {
            recordSelfAssign();
        } else {
            node.recordCollectionOp((collector, path) -> {
                collector.pullArrayValueBatch(path, collection, values);
            });
        }
    }

    private boolean fastDetermineImmutable(Object o) {
        if (o == null) {
            return true;
        }
        return KnownTypes.isSimpleAndImmutableType(o.getClass());
    }
}
