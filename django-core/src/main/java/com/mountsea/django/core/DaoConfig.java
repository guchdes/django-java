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
package com.mountsea.django.core;

import com.mountsea.django.core.cache.CachePlugin;
import com.mountsea.django.core.cache.DocumentCacheConfigs;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author guch
 * @since 3.0.0
 */
@Getter
@ToString
@Builder(builderClassName = "Builder", toBuilder = true)
public class DaoConfig {

    private final String database;

    private final boolean isCacheEnable;

    private final CachePlugin cachePlugin;

    private final AutoEnableUCCodecRegistry codecRegistry;

    private final CollectibleDocumentFactory collectibleDocumentFactory;

    public static class Builder {
        // 默认值
        private boolean isCacheEnable = false;
        private CachePlugin cachePlugin = null;
        private AutoEnableUCCodecRegistry codecRegistry = AutoEnableUCCodecRegistry.DEFAULT_INSTANCE;
        private CollectibleDocumentFactory collectibleDocumentFactory = new DefaultCollectibleDocumentFactory();
    }
}
