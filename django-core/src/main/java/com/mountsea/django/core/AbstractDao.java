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

import com.mongodb.ClientSessionOptions;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mountsea.django.bson.BsonUtils;
import com.mountsea.django.bson.projection.DocumentNodeHelper;
import com.mountsea.django.bson.util.LazyInitializer;
import com.mountsea.django.core.cache.CachePlugin;
import com.mountsea.django.core.exception.ConcurrentSaveDjangoException;
import com.mountsea.django.core.exception.DjangoException;
import com.mountsea.django.core.exception.IllegalIdTypeDjangoException;
import com.mountsea.django.core.model.BulkSaveResult;
import com.mountsea.django.core.model.SaveMode;
import com.mountsea.django.core.model.SaveResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author guch
 * @since 3.0.0
 */
@Slf4j(topic = "com.mountsea.django.core.dao")
public abstract class AbstractDao implements DatabaseMetaDao, KeyOperationalDao {

    protected final SessionBindingMongoDatabase sessionBindingMongoDatabase;

    protected final MongoClient mongoClient;

    protected final MongoDatabase database;

    protected final DaoConfig config;

    protected final LazyInitializer<ClientSession> clientSession;

    protected final DatabaseDaoFactory databaseDaoFactory;

    protected volatile boolean isSessionClosed;

    protected final CachePlugin cachePlugin;

    protected AbstractDao(MongoClient mongoClient, DaoConfig config, DatabaseDaoFactory databaseDaoFactory,
                          @Nullable ClientSessionOptions sessionOptions) {
        this.databaseDaoFactory = databaseDaoFactory;
        this.mongoClient = mongoClient;
        this.config = config;
        this.cachePlugin = config.getCachePlugin();
        this.database = mongoClient.getDatabase(getDatabaseName()).withCodecRegistry(getCodecRegistry());
        if (sessionOptions == null) {
            this.clientSession = null;
            this.sessionBindingMongoDatabase = new SessionBindingMongoDatabase(database, () -> null);
        } else {
            this.clientSession = new LazyInitializer<>(() -> {
                return mongoClient.startSession(sessionOptions);
            });
            this.sessionBindingMongoDatabase = new SessionBindingMongoDatabase(database, () -> {
                if (isSessionClosed) {
                    throw new DjangoException("session closed");
                }
                return clientSession.get();
            });
        }
    }

    protected CodecRegistry getCodecRegistry() {
        return config.getCodecRegistry();
    }

    protected BsonDocument toBsonDocument(Object o) {
        return BsonUtils.toBsonDocument(o, getCodecRegistry());
    }

    protected <T> T fromBsonDocument(BsonDocument bsonDocument, Class<T> tClass) {
        return BsonUtils.fromBsonDocument(bsonDocument, tClass, getCodecRegistry());
    }

    @Override
    public DatabaseDaoFactory getDatabaseDaoFactory() {
        return databaseDaoFactory;
    }

    @Override
    public String getDatabaseName() {
        return config.getDatabase();
    }

    public <T extends CollectibleDocument> SessionBindingMongoCollection<T> getSessionBindingMongoCollection(Class<T> documentClass) {
        return sessionBindingMongoDatabase.getCollection(CollectibleDocumentDefinitions.getDocumentDefinition(documentClass).getCollectionName(),
                documentClass);
    }

    protected <T extends CollectibleDocument> SessionBindingMongoCollection<T> getSessionBindingBsonMongoCollection(Class<T> documentClass) {
        return sessionBindingMongoDatabase.getCollection(CollectibleDocumentDefinitions.getDocumentDefinition(documentClass).getCollectionName(),
                documentClass);
    }

    @Override
    public boolean isCacheEnable() {
        return config.isCacheEnable();
    }

    @SuppressWarnings("unchecked")
    protected <T extends CollectibleDocument> SessionBindingMongoCollection<T> getSessionBindingMongoCollection(T document) {
        return (SessionBindingMongoCollection<T>) getSessionBindingMongoCollection(document.getClass());
    }

    private void addKeyFieldsToWriteContent(WriteContentDocument writeContent, CollectibleDocumentDefinition definition,
                                            CollectibleDocument document) {
        for (Map.Entry<String, CollectibleDocumentDefinition.Property<?>> entry : definition.getKeyDefinition().getPropertyMap().entrySet()) {
            CollectibleDocumentDefinition.Property<?> property = definition.getPropertyMap().get(entry.getKey());
            Object v = property.get(document);
            writeContent.put(entry.getKey(), v);
        }
    }

    private void addFieldsToWriteContent(WriteContentDocument writeContent, CollectibleDocumentDefinition definition,
                                         CollectibleDocument document, List<String> fields) {
        for (String field : fields) {
            CollectibleDocumentDefinition.Property<?> property = definition.getPropertyMap().get(field);
            Object v = property.get(document);
            writeContent.put(field, v);
        }
    }

    /**
     * 获取写入(insert/update)的内容，此方法应该在文档的saveLock加锁中调用
     * (方法执行时是否enableUpdateCollect的状态不会被并发修改，但是文档内容可以被并发修改)。
     *
     * @param resetUpdateRecord 是否本地调用后，重置文档的updateRecord
     * @return 返回WriteContentDocument，返回null表示无需入库。WriteContentDocument入库成功后，库中存储将和本次调用文档加锁recordLock时的状态一致
     */
    @Nullable
    protected WriteContentDocument getWriteContent(CollectibleDocument document, SaveMode saveMode, boolean resetUpdateRecord) {
        if (DocumentNodeHelper.hasEnableUpdateCollect(document)) {
            if (saveMode == SaveMode.INSERT_ONLY) {
                if (!resetUpdateRecord) {
                    return DefaultWriteContentDocument.createInsert(document, getCodecRegistry());
                } else {
                    Object lock = DocumentNodeHelper.getRecordLock(document);
                    synchronized (lock) {
                        DefaultWriteContentDocument insert = DefaultWriteContentDocument.createInsert(document, getCodecRegistry());
                        DocumentNodeHelper.clearUpdateCollector(document);
                        return insert;
                    }
                }
            } else {
                Bson updateRecord = DocumentNodeHelper.getUpdateRecord(document, resetUpdateRecord);
                if (updateRecord == null) {
                    return null;
                }
                WriteContentDocument update = DefaultWriteContentDocument.createUpdate(updateRecord, getCodecRegistry());
                if (update.contains("id")) {
                    update.remove("id");
                }
                if (update.isEmpty()) {
                    return null;
                }
                return update;
            }
        } else {
            if (!resetUpdateRecord) {
                return saveMode == SaveMode.INSERT_ONLY ?
                        DefaultWriteContentDocument.createInsert(document, getCodecRegistry()) :
                        DefaultWriteContentDocument.createUpdate(document, getCodecRegistry());
            } else {
                Object lock = DocumentNodeHelper.getRecordLock(document);
                synchronized (lock) {
                    WriteContentDocument writeContentDocument = saveMode == SaveMode.INSERT_ONLY ?
                            DefaultWriteContentDocument.createInsert(document, getCodecRegistry()) :
                            DefaultWriteContentDocument.createUpdate(document, getCodecRegistry());
                    DocumentNodeHelper.enableUpdateRecord(document);
                    return writeContentDocument;
                }
            }
        }
    }

    private <T extends CollectibleDocument> Bson keyToFilter(CollectibleDocumentDefinition definition, Object key) {
        return definition.getKeyDefinition().getBsonKeyConverter().keyToBsonFilter(key, getCodecRegistry(), definition.isAllowNullKeyField());
    }

    private <T extends CollectibleDocument> Supplier<BsonDocument> keyFilterToValues(CollectibleDocumentDefinition definition,
                                                                                     Bson filter,
                                                                                     Object key) {
        return () -> {
            if (filter instanceof BsonDocument) {
                return (BsonDocument) filter;
            }
            return definition.getKeyDefinition().getBsonKeyConverter().keyToBsonDocument(key, getCodecRegistry(),
                    definition.isAllowNullKeyField());
        };
    }

    @Nullable
    @Override
    public <T extends CollectibleDocument> T findByKey(Class<T> documentClass, Object key) {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        Bson bsonDocument = keyToFilter(definition, key);
        return findInternal(documentClass, bsonDocument, null, definition, true, key);
    }

    @Nullable
    @Override
    public <T extends CollectibleDocument> T findByKey(Class<T> documentClass, Object key, @Nullable Bson projection) {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        Bson bsonDocument = keyToFilter(definition, key);
        return findInternal(documentClass, bsonDocument, projection, definition, false, key);
    }

    @Nullable
    protected abstract <T extends CollectibleDocument> T findInternal(Class<T> documentClass, Bson filter,
                                                                      @Nullable Bson projection, CollectibleDocumentDefinition definition,
                                                                      boolean isCacheInterest, Object cacheKey);

    @Nonnull
    @Override
    public <T extends CollectibleDocument> T getByKey(Class<T> documentClass, Object key) {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        if (!definition.getKeyDefinition().isId()) {
            checkAutoGenerateIdType(definition);
        }
        Bson filter = keyToFilter(definition, key);
        Supplier<BsonDocument> values = keyFilterToValues(definition, filter, key);
        return getInternal(documentClass, filter, null, values, definition, true, key);
    }

    @Nonnull
    @Override
    public <T extends CollectibleDocument> T getByKey(Class<T> documentClass, Object key, @Nullable Bson projection) {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        if (!definition.getKeyDefinition().isId()) {
            checkAutoGenerateIdType(definition);
        }
        Bson filter = keyToFilter(definition, key);
        Supplier<BsonDocument> values = keyFilterToValues(definition, filter, key);
        return getInternal(documentClass, filter, projection, values, definition, false, key);
    }

    /**
     * @param cacheKey 为null时不操作缓存
     */
    @Nonnull
    protected abstract <T extends CollectibleDocument> T getInternal(Class<T> documentClass, Bson filter,
                                                                     @Nullable Bson projection, Supplier<BsonDocument> initDocValues,
                                                                     CollectibleDocumentDefinition definition,
                                                                     boolean isCacheInterest, Object cacheKey);

    protected void setGeneratedObjectId(CollectibleDocument document, CollectibleDocumentDefinition definition, ObjectId objectId) {
        checkAutoGenerateIdType(definition);
        definition.getIdProperty().set(document, objectId);
    }

    protected void checkAutoGenerateIdType(CollectibleDocumentDefinition definition) {
        Class<?> rawClass = definition.getIdProperty().getRawClass();
        if (!(rawClass.equals(ObjectId.class) || rawClass.equals(BsonObjectId.class))) {
            throw new IllegalIdTypeDjangoException("Id type must be ObjectId when using auto generate id.  class: "
                    + definition.getDocumentClass());
        }
    }

    private SaveResult getSaveResult(BulkWriteResult bulkWriteResult, SaveMode saveMode) {
        switch (saveMode) {
            case INSERT_OR_UPDATE:
                return new SaveResult(bulkWriteResult.getMatchedCount() == 0,
                        bulkWriteResult.getMatchedCount() > 0);
            case INSERT_ONLY:
                return new SaveResult(bulkWriteResult.getInsertedCount() > 0, false);
            case UPDATE_ONLY:
                return new SaveResult(false, bulkWriteResult.getModifiedCount() > 0);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public SaveResult saveByKey(CollectibleDocument document) {
        return saveByKey(document, SaveMode.INSERT_OR_UPDATE);
    }

    @Override
    public SaveResult saveByKey(CollectibleDocument document, SaveMode saveMode) {
        Objects.requireNonNull(document);
        try {
            document.getSaveLock().lock();
            BulkSaveResult bulkSaveResult = bulkSave(Collections.singletonList(document), saveMode, true,
                    null, true, false);
            return new SaveResult(bulkSaveResult.getInsertCount() > 0,
                    bulkSaveResult.getUpdateCount() > 0);
        } finally {
            document.getSaveLock().unlock();
        }
    }

    @Override
    public SaveResult insert(CollectibleDocument document) {
        return saveByKey(document, SaveMode.INSERT_ONLY);
    }

    @Override
    public BulkSaveResult bulkSaveByKey(List<? extends CollectibleDocument> list) {
        return bulkSaveByKey(list, SaveMode.INSERT_OR_UPDATE);
    }

    @Override
    public BulkSaveResult bulkSaveByKey(List<? extends CollectibleDocument> list, SaveMode saveMode) {
        return bulkSave(list, saveMode, true, null, true, true);
    }

    private void checkByFields(@Nullable List<String> byFields) {
        if (byFields != null && byFields.contains("id") && byFields.size() > 1) {
            throw new IllegalArgumentException("Fields can only contains id or not contains id");
        }
        if (byFields != null && byFields.contains("_id")) {
            throw new IllegalArgumentException("_id can not be field name. (But the exception is in Bson filter)");
        }
    }

    /**
     * @param checkLock 是否通过tryLock检查是否存在并发，如果tryLock失败则表示存在并发，此时抛出异常
     */
    protected BulkSaveResult bulkSave(List<? extends CollectibleDocument> list, SaveMode saveMode,
                                      boolean isByKey, @Nullable List<String> byFields, boolean isCacheInterest,
                                      boolean checkLock) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (!isByKey && (byFields == null || byFields.isEmpty())) {
            throw new IllegalArgumentException();
        }
        checkByFields(byFields);
        int lockIndex = -1;
        try {
            List<InternalSaveDocument> internalSaveDocuments = new ArrayList<>(list.size());
            CollectibleDocumentDefinition definition = null;
            Class<?> documentClass = null;
            //在入库之前生成的id
            Map<Integer, ObjectId> preGenIds = null;
            BulkWriteResult bulkWriteResult = null;
            int i = 0;
            try {
                for (i = 0; i < list.size(); i++) {
                    CollectibleDocument document = list.get(i);
                    if (checkLock && !document.getSaveLock().tryLock()) {
                        throw new ConcurrentSaveDjangoException();
                    }
                    lockIndex = i;
                    if (definition == null) {
                        documentClass = CollectibleDocumentDefinitions.getRealDocumentClass(document.getClass());
                        definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
                    } else {
                        Class<?> aClass = CollectibleDocumentDefinitions.getRealDocumentClass(document.getClass());
                        if (documentClass != aClass) {
                            throw new IllegalArgumentException("Not same document type in list: "
                                    + documentClass + " - " + aClass);
                        }
                    }
                    //获取并清空文档上的更新内容
                    WriteContentDocument writeContent = getWriteContent(document, saveMode, true);
                    if (writeContent == null) {
                        continue;
                    }
                    boolean byId = (isByKey && definition.getKeyDefinition().isId()) ||
                            (!isByKey && byFields.contains("id"));

                    if ((saveMode == SaveMode.INSERT_ONLY || saveMode == SaveMode.INSERT_OR_UPDATE) && document.getId() == null && !byId) {
                        checkAutoGenerateIdType(definition);
                        if (saveMode == SaveMode.INSERT_ONLY) {
                            //使用insert插入，且id为空，需要先生成id，因为insert插入成功后不会返回服务器生成的id
                            ObjectId objectId = new ObjectId();
                            if (preGenIds == null) {
                                preGenIds = new HashMap<>();
                            }
                            preGenIds.put(i, objectId);
                            writeContent.put("_id", objectId);
                        }
                    }
                    if (!byId && saveMode == SaveMode.INSERT_OR_UPDATE) {
                        //使用upsert时，需要在update中加入key字段，如果key字段只放在filter中，新增插入的文档没有key字段 (id字段例外)
                        if (isByKey) {
                            addKeyFieldsToWriteContent(writeContent, definition, document);
                        } else {
                            addFieldsToWriteContent(writeContent, definition, document, byFields);
                        }
                    }
                    if (isByKey) {
                        Object key = definition.getKeyDefinition().getKeyExtractor().extractKey(document, definition.isAllowNullKeyField());
                        InternalSaveDocument request = new InternalSaveDocument(document,
                                saveMode == SaveMode.INSERT_ONLY ? null : keyToFilter(definition, key),
                                writeContent, key);
                        internalSaveDocuments.add(request);
                    } else {
                        Bson filter = null;
                        if (saveMode != SaveMode.INSERT_ONLY) {
                            List<Bson> fieldEqs = new ArrayList<>();
                            for (String byField : byFields) {
                                CollectibleDocumentDefinition.Property<?> property = definition.getPropertyMap().get(byField);
                                if (property == null) {
                                    throw new IllegalArgumentException("field not found:" + byField + ", in class:" + documentClass);
                                }
                                Object v = property.get(document);
                                fieldEqs.add(Filters.eq(fixIdPropNameFromFieldToBson(byField), v));
                            }
                            filter = Filters.and(fieldEqs);
                        }
                        InternalSaveDocument request = new InternalSaveDocument(document, filter,
                                writeContent, null);
                        internalSaveDocuments.add(request);
                    }
                }

                bulkWriteResult = bulkSaveInternal(internalSaveDocuments, definition, saveMode, isCacheInterest);
            } catch (Throwable t) {
                //入库之前，已加saveLock再从文档对象上获取更新记录并清空记录，重新开始记录，如果入库失败，下次save时获取到的
                //更新记录将不再是准确的(当前文档和库中的差异)，所以入库失败时关闭UpdateRecord，下次入库同步整个文档.
                //为什么不在入库成功后再清空记录？因为加上saveLock并获取记录后，仍然可以修改文档，等入库成功后再清空记录，
                //则清掉的记录和本次入库的会不一致。
                log.error("bulkSaveInternal failed. reset document record state.");
                list.subList(0, Math.min(list.size(), i + 1)).forEach(DocumentNodeHelper::disableUpdateRecord);
                throw t;
            }

            boolean isAllSync = saveMode == SaveMode.INSERT_ONLY ||
                    (bulkWriteResult.getUpserts().size() + bulkWriteResult.getMatchedCount()) == internalSaveDocuments.size();
            if (!isAllSync) {
                log.info("bulkSaveInternal not all document sync. reset document record state.");
                list.forEach(DocumentNodeHelper::disableUpdateRecord);
            }

            for (BulkWriteUpsert upsert : bulkWriteResult.getUpserts()) {
                CollectibleDocument document = list.get(upsert.getIndex());
                BsonValue upsertedId = upsert.getId();
                if (upsertedId instanceof BsonObjectId) {
                    ObjectId objectId = upsertedId.asObjectId().getValue();
                    setGeneratedObjectId(document, definition, objectId);
                } else {
                    log.error("unknown upserted id type {}, {}", upsertedId.getClass(), upsertedId);
                }
            }

            if (preGenIds != null) {
                //入库前生成的id，入库成功后再设置到文档
                CollectibleDocumentDefinition.Property<Object> idProperty = definition.getIdProperty();
                for (Map.Entry<Integer, ObjectId> entry : preGenIds.entrySet()) {
                    CollectibleDocument document = list.get(entry.getKey());
                    idProperty.set(document, entry.getValue());
                }
            }

            return new BulkSaveResult(bulkWriteResult.getInsertedCount() + bulkWriteResult.getUpserts().size(),
                    bulkWriteResult.getModifiedCount(), bulkWriteResult.getDeletedCount());
        } finally {
            if (checkLock) {
                for (int i = 0; i <= lockIndex; i++) {
                    CollectibleDocument document = list.get(i);
                    document.getSaveLock().unlock();
                }
            }
        }
    }

    private String fixIdPropNameFromFieldToBson(String s) {
        return s.equals("id") ? "_id" : s;
    }

    /**
     * 批量保存。list只所有文档必须是同一个文档类。
     *
     * @return 返回每一个request的保存结果
     */
    protected abstract BulkWriteResult bulkSaveInternal(List<InternalSaveDocument> documents, CollectibleDocumentDefinition definition,
                                                        SaveMode saveMode, boolean isCacheInterest);

    @Override
    public boolean deleteByKey(CollectibleDocument document) {
        return bulkDeleteByKey(Collections.singletonList(document)).getDeleteCount() > 0;
    }

    @Override
    public boolean deleteByKey(Class<? extends CollectibleDocument> documentClass, Object key) {
        return bulkDeleteByKey(documentClass, Collections.singletonList(key)).getDeleteCount() > 0;
    }

    protected BulkSaveResult createNoActionBulkSaveResult() {
        return new BulkSaveResult(0, 0, 0);
    }

    @Override
    public BulkSaveResult bulkDeleteByKey(List<? extends CollectibleDocument> document) {
        if (document.isEmpty()) {
            return createNoActionBulkSaveResult();
        }
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(document.get(0).getClass());
        CollectibleDocumentDefinition.KeyExtractor keyExtractor = definition.getKeyDefinition().getKeyExtractor();
        List<InternalDeleteDocument> documents = new ArrayList<>(document.size());
        for (CollectibleDocument dbDocument : document) {
            Object key = keyExtractor.extractKey(dbDocument, true);
            Bson filter = keyToFilter(definition, key);
            documents.add(new InternalDeleteDocument(filter, key));
        }
        int result = bulkDeleteInternal(documents, definition.getDocumentClass(), definition, true, true);
        return new BulkSaveResult(0, 0, result);
    }

    @Override
    public BulkSaveResult bulkDeleteByKey(Class<? extends CollectibleDocument> documentClass, List<Object> keyList) {
        if (keyList.isEmpty()) {
            return createNoActionBulkSaveResult();
        }
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        List<InternalDeleteDocument> documents = new ArrayList<>(keyList.size());
        for (Object key : keyList) {
            Bson filter = keyToFilter(definition, key);
            documents.add(new InternalDeleteDocument(filter, key));
        }
        int result = bulkDeleteInternal(documents, definition.getDocumentClass(), definition, true, true);
        return new BulkSaveResult(0, 0, result);
    }

    abstract protected <T extends CollectibleDocument> int bulkDeleteInternal(List<InternalDeleteDocument> documents,
                                                                              Class<T> documentClass,
                                                                              CollectibleDocumentDefinition definition,
                                                                              boolean isCacheInterest,
                                                                              boolean isByKey);
}
