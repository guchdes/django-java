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
package com.mountsea.django.bson.projection.pojo;

/**
 * Provides access for getting and setting property data.
 *
 * @param <T> the type of the property
 * @since 3.5
 */
public interface PropertyAccessor<T> {

    /**
     * Gets the value for a given PropertyModel instance.
     *
     * @param instance the class instance to get the property value from
     * @param <S>      the class instance type
     * @return the value of the property.
     */
    <S> T get(S instance);

    /**
     * Sets a value on the given PropertyModel
     *
     * @param instance the instance to set the property value to
     * @param <S>      the class instance type
     * @param value    the new value for the property
     */
    <S> void set(S instance, T value);
}
