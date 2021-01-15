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
package io.github.guchdes.django.core.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Base implementation of {@link Constant}.
 */
public abstract class AbstractConstant<T extends AbstractConstant<T>> implements Constant<T> {

    private static final AtomicLong uniqueIdGenerator = new AtomicLong();
    private final int id;
    private final String name;
    private final long uniquifier;

    /**
     * Creates a new instance.
     */
    protected AbstractConstant(int id, String name) {
        this.id = id;
        this.name = name;
        this.uniquifier = uniqueIdGenerator.getAndIncrement();
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final int id() {
        return id;
    }

    @Override
    public final String toString() {
        return name();
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int compareTo(T o) {
        if (this == o) {
            return 0;
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        AbstractConstant<T> other = o;
        int returnCode;

        returnCode = hashCode() - other.hashCode();
        if (returnCode != 0) {
            return returnCode;
        }

        if (uniquifier < other.uniquifier) {
            return -1;
        }
        if (uniquifier > other.uniquifier) {
            return 1;
        }

        throw new Error("failed to compare two different constants");
    }

}
