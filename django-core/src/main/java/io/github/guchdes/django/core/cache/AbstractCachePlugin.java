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
import io.github.guchdes.django.core.CollectibleDocumentDefinition;
import io.github.guchdes.django.core.cache.DocumentCacheConfigs.SingleItemCacheConfig;
import io.github.guchdes.django.core.util.Attribute;
import io.github.guchdes.django.core.util.AttributeKey;

/**
 * @author guch
 * @since 3.0.0
 */
public abstract class AbstractCachePlugin implements CachePlugin {
    public static final AttributeKey<DocumentCacheConfigs.SingleItemCacheConfig> CACHE_CONFIG_KEY = AttributeKey.valueOf("CACHE_CONFIG");

    protected final DocumentCacheConfigs configs;

    protected AbstractCachePlugin(DocumentCacheConfigs configs) {
        this.configs = configs;
        SingleItemCacheConfig globalConfig = configs.getDefaultConfig();
        if (globalConfig.getEnable() == null || globalConfig.getMaxSize() == 0 || globalConfig.getExpireAfterWriteMills() == 0
                || globalConfig.getExpireAfterAccessMills() == 0 || globalConfig.getSoftReference() == null
                || globalConfig.getWeakReference() == null) {
            throw new IllegalArgumentException("Default cache config can not has non-config property");
        }
    }

    @Override
    public final boolean isCacheEnable(CollectibleDocumentDefinition definition) {
        Boolean enable = resolveConfigForClass(definition).getEnable();
        return enable != null && enable;
    }

    protected SingleItemCacheConfig resolveConfigForClass(CollectibleDocumentDefinition definition) {
        Attribute<SingleItemCacheConfig> attr = definition.getAttributeMap().attr(CACHE_CONFIG_KEY);
        SingleItemCacheConfig singleItemCacheConfig = attr.get();
        if (singleItemCacheConfig == null) {
            SingleItemCacheConfig globalConfig = configs.getDefaultConfig();

            Class<? extends CollectibleDocument> documentClass = definition.getDocumentClass();
            SingleItemCacheConfig cacheConfig = configs.getDocumentClassConfig(documentClass);
            EnableDocumentCache documentCache = documentClass.getAnnotation(EnableDocumentCache.class);
            SingleItemCacheConfig annoConfig = documentCache == null ? null :
                    new SingleItemCacheConfig(true, documentCache.maxSize(), documentCache.expireAfterAccessMills(),
                            documentCache.expireAfterWriteMills(), documentCache.weakReference().toBooleanWrapper(), documentCache.softReference().toBooleanWrapper());

            singleItemCacheConfig = mergeConfig(mergeConfig(cacheConfig, annoConfig), globalConfig);
            attr.set(singleItemCacheConfig);
        }
        return singleItemCacheConfig;
    }

    private SingleItemCacheConfig mergeConfig(SingleItemCacheConfig highPri, SingleItemCacheConfig lowPri) {
        if (highPri != null && lowPri != null) {
            return new SingleItemCacheConfig(highPri.getEnable() != null ? highPri.getEnable() : lowPri.getEnable(),
                    highPri.getMaxSize() != 0 ? highPri.getMaxSize() : lowPri.getMaxSize(),
                    highPri.getExpireAfterAccessMills() != 0 ? highPri.getExpireAfterAccessMills() : lowPri.getExpireAfterAccessMills(),
                    highPri.getExpireAfterWriteMills() != 0 ? highPri.getExpireAfterWriteMills() : lowPri.getExpireAfterWriteMills(),
                    highPri.getWeakReference() != null ? highPri.getWeakReference() : lowPri.getWeakReference(),
                    highPri.getSoftReference() != null ? highPri.getSoftReference() : lowPri.getSoftReference());
        } else {
            if (highPri != null) {
                return highPri;
            } else if (lowPri != null) {
                return lowPri;
            } else {
                return null;
            }
        }
    }
}
