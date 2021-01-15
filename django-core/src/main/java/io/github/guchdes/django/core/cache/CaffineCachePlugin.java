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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.guchdes.django.core.CollectibleDocument;
import io.github.guchdes.django.core.CollectibleDocumentDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 使用caffine缓存实现
 *
 * @Author guch
 * @Since 3.0.0
 */
public class CaffineCachePlugin extends AbstractCachePlugin implements CachePlugin {

    private final Map<String, Cache<Object, Object>> map = new ConcurrentHashMap<>();

    public CaffineCachePlugin(DocumentCacheConfigs configs) {
        super(configs);
    }

    @Override
    public CollectibleDocument get(CollectibleDocumentDefinition definition, Object key, Supplier<CollectibleDocument> supplier) {
        Cache<Object, Object> cache = getCache(definition);
        return (CollectibleDocument) cache.get(key, o -> {
            return supplier.get();
        });
    }

    @Override
    public void remove(CollectibleDocumentDefinition definition, Object key) {
        Cache<Object, Object> cache = getCache(definition);
        cache.invalidate(key);
    }

    @Override
    public void save(CollectibleDocumentDefinition definition, Object key, CollectibleDocument dbDocument) {
        Cache<Object, Object> cache = getCache(definition);
        cache.put(key, dbDocument);
    }

    private Cache<Object, Object> getCache(CollectibleDocumentDefinition definition) {
        Class<? extends CollectibleDocument> documentClass = definition.getDocumentClass();
        return map.computeIfAbsent(documentClass.getName(), name -> {
            DocumentCacheConfigs.SingleItemCacheConfig cacheConfig = resolveConfigForClass(definition);
            Caffeine<Object, Object> builder = Caffeine.newBuilder();
            if (cacheConfig.getMaxSize() > 0) {
                builder.maximumSize(cacheConfig.getMaxSize());
            }
            if (cacheConfig.getExpireAfterAccessMills() > 0) {
                builder.expireAfterAccess(cacheConfig.getExpireAfterAccessMills(), TimeUnit.MILLISECONDS);
            }
            if (cacheConfig.getExpireAfterWriteMills() > 0) {
                builder.expireAfterWrite(cacheConfig.getExpireAfterWriteMills(), TimeUnit.MILLISECONDS);
            }
            if (cacheConfig.getSoftReference() != null && cacheConfig.getSoftReference()) {
                builder.softValues();
            }
            if (cacheConfig.getWeakReference() != null && cacheConfig.getWeakReference()) {
                builder.weakValues();
                builder.weakKeys();
            }
            return builder.build();
        });
    }

}
