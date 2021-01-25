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

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author guch
 * @since 3.0.0
 */
public interface MapListener<K, V> extends ContainerListener<V, Map<K, V>> {

    /**
     * 对进入Map的元素做替换
     */
    interface IncomingMapValueTransformer<K, V> {
        /**
         * 此方法是等幂的，不能对元素本身有副作用，调用此方法后，元素不一定会加入容器，
         * 同一个进入容器的元素也可能被多次调用此方法 (但不会用上一次调用返回的结果作为参数)
         *
         * @return 实际被添加的元素
         */
        V transform(K k, V v);
    }

    @Nullable
    IncomingMapValueTransformer<K, V> getIncomingMapValueTransformer();

    /**
     * 添加元素后
     *
     * @param map      包装后的Map
     * @param k        添加的key
     * @param v        添加的value
     * @param previous 以前的value
     */
    void afterPut(Map<K, V> map, K k, V v, V previous);

    /**
     * 移除元素后
     *
     * @param map 包装后的Map
     * @param k
     */
    void afterRemove(Map<K, V> map, Object k, Object removed);

    /**
     * clear之前调用此方法.
     * 一次操作只会调用一个通知方法,且不会重复,调用了此方法就不会调用其他方法,反之亦然.
     *
     * @param map      包装后的container
     */
    void beforeClear(Map<K, V> map);

}
