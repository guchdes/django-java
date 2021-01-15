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

import io.github.guchdes.django.bson.projection.containerlisten.CollectionListener;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @Author guch
 * @Since 3.0.0
 */
class ProxyCollectionListener<E> extends ProxyContainerListener<E, Collection<E>> implements CollectionListener<E> {
    private final IncomingElementTransformer<E> transformer;

    public ProxyCollectionListener(ContainerDocumentNode node) {
        this(node, true);
    }

    public ProxyCollectionListener(ContainerDocumentNode node, boolean allowNullValues) {
        super(node);
        this.transformer = allowNullValues ? null : element -> {
            if (element == null) {
                throw new IllegalArgumentException("Not allow null values in container :" + node.getClass());
            }
            return element;
        };
    }

    @Nullable
    @Override
    public IncomingElementTransformer<E> getIncomingElementTransformer() {
        return transformer;
    }

    protected void processIncomingElement(E e) {
        if (e == null) {
            return;
        }
        if (e instanceof DocumentNode) {
            ((DocumentNode) e).setParent(node, "?");
        }
    }

    protected void processLeaveElement(Object e) {
        if (e == null) {
            return;
        }
        if (e instanceof DocumentNode) {
            ((DocumentNode) e).unsetParent(node);
        }
    }

    @Override
    public void afterAdd(Collection<E> collection, E value, boolean isTail) {
        processIncomingElement(value);
        if (isTail) {
            node.recordCollectionOp((collector, path) -> {
                collector.pushArrayValue(path, collection, value);
            });
        } else {
            recordSelfAssign();
        }
    }

    @Override
    public void afterAddAll(Collection<E> collection, Collection<? extends E> values, boolean isTail) {
        for (E e : values) {
            processIncomingElement(e);
        }
        if (isTail) {
            node.recordCollectionOp((collector, path) -> {
                collector.pushArrayValueBatch(path, collection, values);
            });
        } else {
            recordSelfAssign();
        }
    }

    @Override
    public void afterRemove(Collection<E> collection, Object value) {
        processLeaveElement(value);
        //只有set中remove才是pull操作
        recordSelfAssign();
    }

    @Override
    public void afterRemoveAll(Collection<E> collection, Collection<?> values) {
        for (Object value : values) {
            processLeaveElement(value);
        }
        recordSelfAssign();
    }

    @Override
    public void afterClear(Collection<E> container, List<E> removed) {
        for (E e : removed) {
            processLeaveElement(e);
        }
        recordSelfAssign();
    }

    protected void recordSelfAssign() {
        node.recordSelfAssign();
    }
}
