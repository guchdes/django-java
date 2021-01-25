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
package com.mountsea.django.bson.projection.containerlisten;

import java.util.Collection;

/**
 * @author guch
 * @since 3.0.0
 */
public interface SetListener<T> extends CollectionListener<T> {


    /**
     * 只有add成功(使set的元素发生改变)后调用此方法
     *
     * @param collection 包装后的collection
     * @param value      add成功的元素
     * @param isTail     是否确定在尾部添加 (调用 {@link Collection#add}就算尾部)
     */
    @Override
    void afterAdd(Collection<T> collection, T value, boolean isTail);

    /**
     * 只对addAll的成功(使set的元素发生改变)的元素调用此方法
     *
     * @param collection 包装后的collection
     * @param values     addAll成功的元素
     * @param isTail     是否在确定尾部添加 (调用 {@link Collection#add}就算尾部)
     */
    @Override
    void afterAddAll(Collection<T> collection, Collection<? extends T> values, boolean isTail);
}
