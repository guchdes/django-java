/**
 * MIT License
 *
 * Copyright (c) 2021 fengniao studio
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
package io.github.guchdes.django.core.util;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ArrayAttributeMap，在Attribute已存在时attr方法无锁。
 * AttributeKey总数量有限时使用。
 */
public class ArrayAttributeMap implements AttributeMap {

    private volatile Object[] array = new Object[0];

    @Override
    @SuppressWarnings("unchecked")
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        Object[] array = this.array;
        int id = key.id();
        if (id >= array.length) {
            //数组只会增长，增长时复制原先的元素
            synchronized (this) {
                array = this.array;
                if (id >= array.length) {
                    Object[] newArray = new Object[id + 1];
                    System.arraycopy(array, 0, newArray, 0, array.length);
                    array = this.array = newArray;
                }
            }
        }

        Object o = array[id];
        if (o == null) {
            synchronized (this) {
                array = this.array;
                o = array[id];
                if (o == null) {
                    o = array[id] = new DefaultAttribute<>(key);
                }
            }
        }

        return (Attribute<T>) o;
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        Object[] array = this.array;
        int id = key.id();
        return id < array.length && array[id] != null;
    }

    private static final class DefaultAttribute<T> extends AtomicReference<T> implements Attribute<T> {

        private final AttributeKey<T> key;

        DefaultAttribute(AttributeKey<T> key) {
            this.key = key;
        }

        @Override
        public AttributeKey<T> key() {
            return key;
        }

        @Override
        public T setIfAbsent(T value) {
            while (!compareAndSet(null, value)) {
                T old = get();
                if (old != null) {
                    return old;
                }
            }
            return null;
        }

        @Override
        public T getAndRemove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
