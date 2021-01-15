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
package io.github.guchdes.django.core.cache;

import io.github.guchdes.django.core.CollectibleDocument;
import lombok.*;

import javax.annotation.Nullable;

/**
 * 缓存配置查找优先级： {@link DocumentCacheConfigs}对单个类型的配置 -> 类型的EnableDocumentCache注解配置 -> {@link DocumentCacheConfigs}的全局配置
 *
 * @Author guch
 * @Since 3.0.0
 */
public interface DocumentCacheConfigs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class SingleItemCacheConfig {
        /**
         * 是否启用缓存
         * <p>
         * null表示未配置(使用默认的全局配置)。
         */
        private Boolean enable;

        /**
         * 最大缓存数量，-1表示无限制。
         * <p>
         * 0表示未配置(使用默认的全局配置)。
         */
        private int maxSize;

        /**
         * 访问(包括创建，读取，更新..)后多久移除缓存，-1表示不自动移除。
         * <p>
         * 0表示未配置(使用默认的全局配置)。
         */
        private long expireAfterAccessMills;

        /**
         * 更新/写入后多久移除缓存，-1表示不自动移除。
         * 和expireAfterAccessMills互不影响，各自独立计算超时时间，其中一个超时后即移除缓存。
         * <p>
         * 0表示未配置(使用默认的全局配置)。
         */
        private long expireAfterWriteMills;

        /**
         * 是否使用WeakReference
         * <p>
         * null表示未配置(使用默认的全局配置)。
         */
        private Boolean weakReference;

        /**
         * 是否使用SoftReference。不能和WeakReference同时使用。
         * <p>
         * null表示未配置(使用默认的全局配置)。
         */
        private Boolean softReference;
    }

    SingleItemCacheConfig DEFAULT_DEFAULT_CONFIG = new SingleItemCacheConfig(false, 1000,
            -1L, -1L, false, true);

    /**
     * 返回默认全局配置。默认配置中，{@link SingleItemCacheConfig}的方法不能返回null。
     */
    SingleItemCacheConfig getDefaultConfig();

    /**
     * 返回单个文档类的配置
     */
    SingleItemCacheConfig getDocumentClassConfig(Class<? extends CollectibleDocument> aClass);

}
