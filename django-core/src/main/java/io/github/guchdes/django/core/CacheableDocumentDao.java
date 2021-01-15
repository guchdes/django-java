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
package io.github.guchdes.django.core;

import com.mongodb.ClientSessionOptions;
import com.mongodb.MongoException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClient;
import io.github.guchdes.django.core.cache.CachePlugin;
import io.github.guchdes.django.core.model.SaveMode;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * @Author guch
 * @Since 3.0.0
 */
@Slf4j(topic = "io.github.guchdes.django.core.dao")
public class CacheableDocumentDao extends KeyOperationalDaoImpl {

    public CacheableDocumentDao(MongoClient mongoClient, DaoConfig config, DatabaseDaoFactory databaseDaoFactory,
                                @Nullable ClientSessionOptions sessionOptions) {
        super(mongoClient, config, databaseDaoFactory, sessionOptions);
    }

    private CachePlugin getCachePlugin() {
        return cachePlugin;
    }

    @SuppressWarnings("unchecked")
    private boolean isDocumentCacheable(CollectibleDocumentDefinition definition) {
        if (!isCacheEnable()) {
            return false;
        }
        return cachePlugin != null && cachePlugin.isCacheEnable(definition);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    protected <T extends CollectibleDocument> T findInternal(Class<T> documentClass, Bson filter,
                                                             @Nullable Bson projection, CollectibleDocumentDefinition definition,
                                                             boolean isCacheInterest, Object cacheKey) {
        if (!isCacheInterest || !isDocumentCacheable(definition)) {
            return super.findInternal(documentClass, filter, projection, definition, isCacheInterest, cacheKey);
        }
        return (T) getCachePlugin().get(definition, cacheKey, () -> {
            return super.findInternal(documentClass, filter, projection, definition, isCacheInterest, cacheKey);
        });
    }

    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    protected <T extends CollectibleDocument> T getInternal(Class<T> documentClass, Bson filter,
                                                            @Nullable Bson projection, Supplier<BsonDocument> initDocValues,
                                                            CollectibleDocumentDefinition definition,
                                                            boolean isCacheInterest, Object cacheKey) {
        if (!isCacheInterest || !isDocumentCacheable(definition)) {
            return super.getInternal(documentClass, filter, projection, initDocValues, definition, isCacheInterest, cacheKey);
        }
        return (T) getCachePlugin().get(definition, cacheKey, () -> {
            return super.getInternal(documentClass, filter, projection, initDocValues, definition, isCacheInterest, cacheKey);
        });
    }

    @Override
    protected BulkWriteResult bulkSaveInternal(List<InternalSaveDocument> documents, CollectibleDocumentDefinition definition,
                                               SaveMode saveMode, boolean isCacheInterest) {
        boolean isCache = isCacheInterest && isDocumentCacheable(definition);
        try {
            BulkWriteResult bulkWriteResult = super.bulkSaveInternal(documents, definition, saveMode, isCacheInterest);
            if (isCache) {
                for (InternalSaveDocument document : documents) {
                    getCachePlugin().save(definition, document.getCacheKey(), document.getDocument());
                }
            }
            return bulkWriteResult;
        } catch (MongoException e) {
            //报错时清空缓存
            if (isCache) {
                for (InternalSaveDocument document : documents) {
                    getCachePlugin().remove(definition, document.getCacheKey());
                }
            }
            throw e;
        }
    }

    @Override
    protected <T extends CollectibleDocument> int bulkDeleteInternal(List<InternalDeleteDocument> documents,
                                                                     Class<T> documentClass,
                                                                     CollectibleDocumentDefinition definition,
                                                                     boolean isCacheInterest,
                                                                     boolean isByKey) {
        int r = super.bulkDeleteInternal(documents, documentClass, definition, isCacheInterest, isByKey);
        if (isCacheInterest && isDocumentCacheable(definition)) {
            for (InternalDeleteDocument document : documents) {
                getCachePlugin().remove(definition, document.getCacheKey());
            }
        }
        return r;
    }

}
