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
package com.mountsea.django.bson.util;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Represents a function that accepts zero arguments and returns some value.
 * Function might throw a checked exception instance.
 *
 * @param <T> the type of the output to the function
 * @param <E> the type of the thrown checked exception
 * @author Grzegorz Piwowarek
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;

    static <T> Supplier<T> unchecked(ThrowingSupplier<T, ?> supplier) {
        return requireNonNull(supplier).uncheck();
    }

    static <T> Supplier<Optional<T>> lifted(ThrowingSupplier<T, ?> supplier) {
        return requireNonNull(supplier).lift();
    }

    /**
     * @return a new Supplier instance which wraps thrown checked exception instance into a RuntimeException
     */
    default Supplier<T> uncheck() {
        return () -> {
            try {
                return get();
            } catch (final Exception e) {
                throw new WrappedException(e);
            }
        };
    }

    /**
     * @return a new Supplier that returns the result as an Optional instance. In case of a failure or null, empty Optional is
     * returned
     */
    default Supplier<Optional<T>> lift() {
        return () -> {
            try {
                return Optional.ofNullable(get());
            } catch (final Exception e) {
                return Optional.empty();
            }
        };
    }
}
