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
package io.github.guchdes.django.bson.projection.containerlisten;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * 容器监听器，监控元素的进出
 *
 * @Author guch
 * @Since 3.0.0
 */
public interface CollectionListener<E> extends ContainerListener<E, Collection<E>> {

    /**
     * 对进入容器的元素做替换
     */
    interface IncomingElementTransformer<T> {
        /**
         * 此方法是等幂的，不能对元素本身有副作用，调用此方法后，元素不一定会加入容器，
         * 同一个进入容器的元素也可能被多次调用此方法 (但不会用上一次调用返回的结果作为参数)
         *
         * @return 实际被添加的元素
         */
        T transform(T element);
    }

    @Nullable
    IncomingElementTransformer<E> getIncomingElementTransformer();

    /**
     * 添加元素后
     *
     * @param collection 包装后的collection
     * @param value
     * @param isTail     是否确定在尾部添加 (调用 {@link Collection#add}就算尾部)，
     *                   如果是不确定是否在尾部（比如通过subList添加），则传递false。
     */
    void afterAdd(Collection<E> collection, E value, boolean isTail);

    /**
     * 添加元素后
     *
     * @param collection 包装后的collection
     * @param values
     * @param isTail     是否在确定尾部添加 (调用 {@link Collection#add}就算尾部)，
     *                   如果是不确定是否在尾部（比如通过subList添加），则传递false。
     */
    void afterAddAll(Collection<E> collection, Collection<? extends E> values, boolean isTail);

    /**
     * 移除元素后
     *
     * @param collection 包装后的collection
     * @param value
     */
    void afterRemove(Collection<E> collection, Object value);

    /**
     * 移除元素后
     *
     * @param collection 包装后的collection
     * @param values
     */
    void afterRemoveAll(Collection<E> collection, Collection<?> values);

    /**
     * clear之前调用此方法.
     * 从subList调用clear时，通知afterRemoveAll.
     * <p>
     * 一次操作只会调用一个通知方法,且不会重复,调用了此方法就不会调用其他方法,反之亦然.
     *
     * @param container 包装后的container
     * @param removed   clear时被移除的元素
     */
    void afterClear(Collection<E> container, List<E> removed);

}
