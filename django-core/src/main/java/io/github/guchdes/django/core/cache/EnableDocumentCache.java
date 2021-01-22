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
import io.github.guchdes.django.core.spring.BoolValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在 {@link CollectibleDocument}类型上注解，表示该类使用缓存
 * <p>
 * 缓存配置查找优先级： {@link DocumentCacheConfigs}对单个类型的配置 -> 类型的@EnableDocumentCache注解配置 -> {@link DocumentCacheConfigs}的全局配置
 * <p>
 * 注解处理和配置优先级的实现在{@link AbstractCachePlugin}类中，如果是自定义的CachePlugin，需要继承AbstractCachePlugin或者另行实现缓存逻辑。
 *
 * @author guch
 * @since 3.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableDocumentCache {

    /**
     * 最大缓存数量，-1表示无限制。
     * <p>
     * 0表示未配置(使用默认的全局配置)。
     */
    int maxSize() default 0;

    /**
     * 访问(包括创建，读取，更新..)后多久移除缓存，-1表示不自动移除。
     * <p>
     * 0表示未配置(使用默认的全局配置)。
     */
    long expireAfterAccessMills() default 0;

    /**
     * 更新/写入后多久移除缓存，-1表示不自动移除。
     * 和expireAfterAccessMills互不影响，各自独立计算超时时间，其中一个超时后即移除缓存。
     * <p>
     * 0表示未配置(使用默认的全局配置)。
     */
    long expireAfterWriteMills() default 0;

    /**
     * 是否使用WeakReference
     * <p>
     * DEFAULT表示未配置(使用默认的全局配置)。
     */
    BoolValue weakReference() default BoolValue.DEFAULT;

    /**
     * 是否使用SoftReference。不能和WeakReference同时使用。
     * <p>
     * null表示未配置(使用默认的全局配置)。
     */
    BoolValue softReference() default BoolValue.DEFAULT;
}
