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
package io.github.guchdes.django.core.cache;

import io.github.guchdes.django.core.CollectibleDocument;
import io.github.guchdes.django.core.CollectibleDocumentDefinition;

import java.util.function.Supplier;

/**
 * @Author guch
 * @Since 3.0.0
 */
public interface CachePlugin {

    boolean isCacheEnable(CollectibleDocumentDefinition definition);

    /**
     * 查找，如果不存在则调用supplier获取对象，并加入缓存。
     * 实现上要并发安全，多线程对用一个key调用时，要确保获取到的是同一个对象。
     *
     * @param supplier 可以返回null值，如果返回null则不加入缓存
     */
    CollectibleDocument get(CollectibleDocumentDefinition definition, Object key, Supplier<CollectibleDocument> supplier);

    /**
     * 从缓存移除
     */
    void remove(CollectibleDocumentDefinition definition, Object key);

    /**
     * 文档新增插入或更新。
     */
    void save(CollectibleDocumentDefinition definition, Object key, CollectibleDocument dbDocument);
}
