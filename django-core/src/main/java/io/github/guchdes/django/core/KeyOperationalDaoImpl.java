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
package io.github.guchdes.django.core;

import com.mongodb.ClientSessionOptions;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.github.guchdes.django.bson.BsonUtils;
import io.github.guchdes.django.core.exception.DjangoException;
import io.github.guchdes.django.core.model.SaveMode;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @Author guch
 * @Since 3.0.0
 */
@Slf4j(topic = "io.github.guchdes.django.core.dao")
class KeyOperationalDaoImpl extends AbstractDao implements KeyOperationalDao {

    public KeyOperationalDaoImpl(MongoClient mongoClient, DaoConfig config, DatabaseDaoFactory databaseDaoFactory,
                                 @Nullable ClientSessionOptions sessionOptions) {
        super(mongoClient, config, databaseDaoFactory, sessionOptions);
    }

    @Override
    protected <T extends CollectibleDocument> T findInternal(Class<T> documentClass, Bson filter,
                                                             @Nullable Bson projection, CollectibleDocumentDefinition definition,
                                                             boolean isCacheInterest, Object cacheKey) {
        return findInternal(getSessionBindingMongoCollection(documentClass), documentClass, filter, projection);
    }

    private <T extends CollectibleDocument> T findInternal(SessionBindingMongoCollection<T> collection, Class<T> documentClass,
                                                           Bson filter, @Nullable Bson projection) {
        FindIterable<T> iterable = collection.find(filter, documentClass);
        if (projection != null) {
            iterable.projection(projection);
        }
        T result = DBUtils.getSingleResult(iterable);
        if (log.isDebugEnabled()) {
            log.debug("find: collection {}, filter {}, projection{}, result{}", collection.getNamespace(),
                    toBsonDocumentForLog(filter), toBsonDocumentForLog(projection),
                    toBsonDocumentForLog(projection));
        }
        return result;
    }

    @Override
    @Nonnull
    protected <T extends CollectibleDocument> T getInternal(Class<T> documentClass, Bson filter,
                                                            @Nullable Bson projection, Supplier<BsonDocument> initDocValues,
                                                            CollectibleDocumentDefinition definition,
                                                            boolean isCacheInterest, Object cacheKey) {
        SessionBindingMongoCollection<T> collection = getSessionBindingMongoCollection(documentClass);
        T find = findInternal(collection, documentClass, filter, projection);
        if (find != null) {
            return find;
        }
        BsonDocument initDocValuesDocument = initDocValues.get();
        T insert = insertIfNotFind(collection, documentClass, filter, initDocValuesDocument, definition);
        if (insert == null) {
            // 可能是被其他线程或进程先初始化
            find = findInternal(collection, documentClass, filter, projection);
            if (find == null) {
                throw new DjangoException("insert failed during get():" + filter.toString());
            } else {
                return find;
            }
        } else {
            return insert;
        }
    }

    /**
     * 初始化插入, 如果插入成功则返回新插入的文档，否则返回null.
     */
    @Nullable
    protected <T extends CollectibleDocument> T insertIfNotFind(SessionBindingMongoCollection<T> collection, Class<T> documentClass,
                                                                Bson filters, BsonDocument initDocValues, CollectibleDocumentDefinition definition) {
        T document = BsonUtils.fromBsonDocument(initDocValues, documentClass);
        document.initForStore();
        BsonDocument bsonDocument = toBsonDocument(document);
        if (bsonDocument.get("_id") == null && !(definition.getIdProperty().getType().equals(ObjectId.class) ||
                definition.getIdProperty().getType().equals(BsonObjectId.class))) {
            throw new DjangoException("_id is null and cannot auto generate when inserting");
        }
        // 仅当不存在则插入
        UpdateResult updateResult = collection.updateOne(filters, Updates.setOnInsert(bsonDocument),
                DBUtils.UPSERT_OPTION);
        if (updateResult.getUpsertedId() == null) {
            log.debug("insertIfNotFind failed: collection: {}, filters: {}, initDocValues: {}", collection.getNamespace(),
                    toBsonDocumentForLog(filters), initDocValues);
            return null;
        } else {
            bsonDocument.put("_id", updateResult.getUpsertedId());
            T bsonDocument1 = fromBsonDocument(bsonDocument, documentClass);
            log.debug("insertIfNotFind success: collection: {}, filters: {}, initDocValues: {}, result {}",
                    collection.getNamespace(), toBsonDocumentForLog(filters), initDocValues, bsonDocument1);
            return bsonDocument1;
        }
    }

    private UpdateOptions getUpsertOption() {
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(true);
        return updateOptions;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected BulkWriteResult bulkSaveInternal(List<InternalSaveDocument> documents, CollectibleDocumentDefinition definition,
                                               SaveMode saveMode, boolean isCacheInterest) {
        List<WriteModel> writeModelList = new ArrayList<>(documents.size());
        for (InternalSaveDocument saveDocument : documents) {
            if (saveDocument == null) {
                continue;
            }
            WriteContentDocument writeContent = saveDocument.getWriteContent();
            switch (saveMode) {
                case INSERT_OR_UPDATE:
                    writeModelList.add(new UpdateOneModel<>(saveDocument.getFilter(), writeContent.getAsBson(),
                            getUpsertOption()));
                    break;
                case INSERT_ONLY:
                    writeModelList.add(new InsertOneModel<>(writeContent));
                    break;
                case UPDATE_ONLY:
                    writeModelList.add(new UpdateOneModel<>(saveDocument.getFilter(), writeContent.getAsBson()));
                    break;
            }
        }

        if (writeModelList.isEmpty()) {
            return new NoActionBultWriteResult();
        }

        SessionBindingMongoCollection mongoCollection = getSessionBindingMongoCollection(definition.getDocumentClass());
        return mongoCollection.bulkWrite(writeModelList);
    }

    @Override
    protected <T extends CollectibleDocument> int bulkDeleteInternal(List<InternalDeleteDocument> documents,
                                                                     Class<T> documentClass,
                                                                     CollectibleDocumentDefinition definition,
                                                                     boolean isCacheInterest,
                                                                     boolean isByKey) {
        if (documents.isEmpty()) {
            return 0;
        }
        SessionBindingMongoCollection<T> collection = getSessionBindingMongoCollection(documentClass);
        if (isByKey && definition.getKeyDefinition().getPropertyMap().size() == 1 && documents.size() > 1) {
            //如果filter字段只有一个，优化为deleteMany
            List<BsonValue> values = new ArrayList<>();
            String prop = definition.getKeyDefinition().getPropertyMap().keySet().iterator().next();
            for (InternalDeleteDocument document : documents) {
                Bson filter = document.getFilter();
                BsonDocument bsonDocument = null;
                if (filter instanceof BsonDocument) {
                    bsonDocument = (BsonDocument) filter;
                } else {
                    bsonDocument = BsonUtils.toBsonDocument(filter, getCodecRegistry());
                }
                BsonValue bsonValue = bsonDocument.get(prop);
                if (bsonValue == null) {
                    throw new DjangoException("Not found key property in filter:" + prop);
                }
                values.add(bsonValue);
            }
            Bson in = Filters.in(prop, values);
            DeleteResult result = collection.deleteMany(in);
            if (log.isDebugEnabled()) {
                log.debug("delete using deleteMany: documentClass :{}, filters :{}, delete count:{}",
                        documentClass, toBsonDocumentForLog(in), result.getDeletedCount());
            }
            return (int) result.getDeletedCount();
        } else {
            List<DeleteOneModel<T>> models = documents.stream().map(x -> new DeleteOneModel<T>(x.getFilter()))
                    .collect(Collectors.toList());
            BulkWriteResult result = collection.bulkWrite(models);
            if (log.isDebugEnabled()) {
                List<BsonDocument> filters = documents.stream().map(x -> toBsonDocumentForLog(x.getFilter()))
                        .collect(Collectors.toList());
                log.debug("delete using bulkWrite: documentClass :{}, filters :{}, delete count:{}",
                        documentClass, filters, result.getDeletedCount());
            }
            return result.getDeletedCount();
        }
    }

    private BsonDocument toBsonDocumentForLog(Object o) {
        try {
            return BsonUtils.toBsonDocument(o, getCodecRegistry());
        } catch (Exception e) {
            log.error("toBsonDocument", e);
            return null;
        }
    }

}
