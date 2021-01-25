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
 * Provides access for setting data and the creation of a class instances.
 *
 * @param <T> the type of the class
 * @since 3.5
 */
public interface InstanceCreator<T> {

    /**
     * Sets a value for the given PropertyModel
     *
     * @param value      the new value for the property
     * @param propertyModel the PropertyModel representing the property to set the value for.
     * @param <S>        the PropertyModel's type
     */
    <S> void set(S value, PropertyModel<S> propertyModel);

    /**
     * Returns the new instance of the class.
     * <p>Note: This will be called after all the values have been set.</p>
     *
     * @return the new class instance.
     */
    T getInstance();

}
