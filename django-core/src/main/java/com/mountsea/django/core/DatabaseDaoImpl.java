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
import com.mongodb.TransactionOptions;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import com.mountsea.django.bson.BsonPathUtils;
import com.mountsea.django.bson.projection.DocumentClassDefinitionException;
import com.mountsea.django.bson.projection.DocumentNodeHelper;
import com.mountsea.django.core.exception.ConcurrentSaveDjangoException;
import com.mountsea.django.core.exception.DjangoException;
import com.mountsea.django.core.exception.DocumentNoIdDjangoException;
import com.mountsea.django.core.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static com.mountsea.django.core.model.MongoModels._id;
import static com.mountsea.django.core.model.MongoModels.idEquals;

/**
 * @author guch
 * @since 3.0.0
 */
@Slf4j(topic = "com.mountsea.django.core.dao")
class DatabaseDaoImpl extends CacheableDocumentDao implements DatabaseDao {

    public DatabaseDaoImpl(MongoClient mongoClient, DaoConfig config, DatabaseDaoFactory databaseDaoFactory,
                           @Nullable ClientSessionOptions sessionOptions) {
        super(mongoClient, config, databaseDaoFactory, sessionOptions);
    }

    @Nonnull
    @Override
    public <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters) {
        return findAllInternal(documentClass, filters, null, 0, -1, null);
    }

    @Nonnull
    @Override
    public <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters, @Nullable Bson projection) {
        return findAllInternal(documentClass, filters, projection, 0, -1, null);
    }

    @Nonnull
    @Override
    public <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters, @Nullable Bson projection,
                                                           int skip, int limit) {
        return findAllInternal(documentClass, filters, projection, skip, limit, null);
    }

    @Nonnull
    @Override
    public <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters, @Nullable Bson projection,
                                                           Bson sort) {
        return findAllInternal(documentClass, filters, projection, 0, -1, sort);
    }

    /**
     * @param documentClass documentClass
     * @param filters       查找条件
     * @param projection    选择返回字段，为空则返回全部字段
     * @param skip          跳过（实现分页）
     * @param limit         数量限制（实现分页）
     * @param sort          排序
     */
    @Nonnull
    @Override
    public <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters, @Nullable Bson projection,
                                                           int skip, int limit, Bson sort) {
        return findAllInternal(documentClass, filters, projection, skip, limit, sort);
    }

    @Nullable
    @Override
    public <T extends CollectibleDocument> T findOne(Class<T> documentClass, Bson filters) {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        return findInternal(documentClass, filters, null, definition,
                false, null);
    }

    @Nullable
    @Override
    public <T extends CollectibleDocument> T findOne(Class<T> documentClass, Bson filters, @Nullable Bson projection) {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        return findInternal(documentClass, filters, projection, definition,
                false, null);
    }

    @Nonnull
    @Override
    public <T extends CollectibleDocument> T getByFields(Class<T> documentClass, MultiEquals multiEquals) {
        return getByFields(documentClass, multiEquals, null);
    }

    @Nonnull
    @Override
    public <T extends CollectibleDocument> T getByFields(Class<T> documentClass, MultiEquals multiEquals, @Nullable Bson projection) {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        Class<?> type = definition.getIdProperty().getRawClass();
        if (!multiEquals.getAllFields().containsKey("_id")) {
            checkAutoGenerateIdType(definition);
        }
        return getInternal(documentClass, multiEquals, projection,
                () -> multiEquals.toBsonDocument(), definition, false, null);
    }

    @Nonnull
    private <T extends CollectibleDocument> List<T> findAllInternal(Class<T> documentClass, Bson filters, @Nullable Bson projection,
                                                                    int skip, int limit, @Nullable Bson sort) {
        SessionBindingMongoCollection<T> mongoCollection = getSessionBindingMongoCollection(documentClass);
        FindIterable<T> iterable = mongoCollection.find(filters);
        iterable.projection(projection);
        iterable.skip(skip);
        if (limit > 0) {
            iterable.limit(limit);
        }
        if (sort != null) {
            iterable.sort(sort);
        }
        return DBUtils.toList(iterable);
    }

    @Override
    public boolean deleteOne(Class<? extends CollectibleDocument> documentClass, Bson filters, @Nullable DeleteOptions deleteOptions) {
        return getSessionBindingMongoCollection(documentClass)
                .deleteOne(filters, deleteOptions == null ? new DeleteOptions() : deleteOptions).getDeletedCount() > 0;
    }

    @Override
    public long deleteMany(Class<? extends CollectibleDocument> documentClass, Bson filters, @Nullable DeleteOptions deleteOptions) {
        return getSessionBindingMongoCollection(documentClass)
                .deleteMany(filters, deleteOptions == null ? new DeleteOptions() : deleteOptions).getDeletedCount();
    }

    @Override
    public boolean updateOne(Class<? extends CollectibleDocument> documentClass, Bson filter, Bson update,
                             @Nullable UpdateOptions updateOptions) {
        SessionBindingMongoCollection<? extends CollectibleDocument> collection = getSessionBindingMongoCollection(documentClass);
        UpdateResult result = collection.updateOne(filter, update, updateOptions == null ? new UpdateOptions() : updateOptions);
        return result.getModifiedCount() > 0;
    }

    @Override
    public UpdateResult updateMany(Class<? extends CollectibleDocument> documentClass, Bson filter,
                                   Bson update, @Nullable UpdateOptions updateOptions) {
        SessionBindingMongoCollection<? extends CollectibleDocument> collection = getSessionBindingMongoCollection(documentClass);
        return collection.updateMany(filter, update, updateOptions == null ? new UpdateOptions() : updateOptions);
    }

    @Override
    public SaveResult saveByFields(CollectibleDocument document, SaveMode saveMode, List<String> fields) {
        Objects.requireNonNull(document);
        Lock saveLock = document.getSaveLock();
        try {
            saveLock.lock();
            BulkSaveResult bulkSaveResult = bulkSave(Collections.singletonList(document), saveMode, false, fields,
                    false, false);
            return new SaveResult(bulkSaveResult.getInsertCount() > 0, bulkSaveResult.getUpdateCount() > 0);
        } finally {
            saveLock.unlock();
        }
    }

    @Override
    public BulkSaveResult bulkSaveByFields(List<? extends CollectibleDocument> documents, SaveMode saveMode, List<String> fields) {
        if (fields.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return bulkSave(documents, saveMode, false, fields, false, true);
    }

    @Override
    public <T extends CollectibleDocument> long atomicIncAndGetField(Class<T> documentClass, Object id, String fieldName, long inc) {
        SessionBindingMongoCollection<BsonDocument> collection = sessionBindingMongoDatabase.getCollection(
                CollectibleDocumentDefinitions.getDocumentDefinition(documentClass).getCollectionName(),
                BsonDocument.class);
        Bson update = Updates.inc(fieldName, inc);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.projection(MongoModels.projectionByFields(fieldName));
        options.returnDocument(ReturnDocument.AFTER);
        BsonDocument doc = collection.findOneAndUpdate(idEquals(id), update, options);
        if (doc == null) {
            // 初始化插入，重新执行
            getByFields(documentClass, MultiEquals.withId(id));
            doc = collection.findOneAndUpdate(idEquals(id), update, options);
        }
        BsonValue bsonValue = BsonPathUtils.getPathElement(doc, fieldName);
        if (bsonValue == null) {
            throw new DjangoException("no return value in path:" + doc);
        }
        return bsonValue.asNumber().longValue();
    }

    @Override
    @Nullable
    public <T extends CollectibleDocument> T atomicTransformDocument(Class<T> documentClass, Object id, @Nullable T firstDocument,
                                                                     List<String> fieldNames,
                                                                     AtomicDocumentTransformer<T> transformer) {
        SessionBindingMongoCollection<BsonDocument> mongoCollection = sessionBindingMongoDatabase.getCollection(
                CollectibleDocumentDefinitions.getDocumentDefinition(documentClass).getCollectionName(),
                BsonDocument.class);
        List<String> projectionFields = new ArrayList<>(fieldNames);
        projectionFields.add(_id);
        Bson projection = MongoModels.projectionByFields(projectionFields);
        T document = firstDocument;
        while (true) {
            if (document == null) {
                document = getByFields(documentClass, MultiEquals.withId(id), projection);
            }
            T newDocument = transformer.accept(document);
            if (newDocument == null) {
                return null;
            }

            // filter and update
            BsonDocument bsonDocument = toBsonDocument(document);
            Document filter = new Document(_id, id);
            for (String fieldName : fieldNames) {
                filter.put(fieldName, BsonPathUtils.getPathElement(bsonDocument, fieldName));
            }
            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
            options.projection(projection);
            options.returnDocument(ReturnDocument.AFTER);
            BsonDocument newBsonDocument = toBsonDocument(newDocument);
            newBsonDocument.remove(_id);
            BsonDocument after = mongoCollection.findOneAndUpdate(filter,
                    new BsonDocument("$set", newBsonDocument), options);

            if (after != null) {
                return fromBsonDocument(after, documentClass);
            } else {
                // 竞争失败，重新获取
                document = getByFields(documentClass, MultiEquals.withId(id), projection);
            }
        }
    }

    @Override
    public boolean casUpdateByVersion(CollectibleDocument document) throws DocumentNoIdDjangoException {
        return casUpdateByVersion(document, getDefaultVersionField(document.getClass()));
    }

    @SuppressWarnings("unchecked")
    private CollectibleDocumentDefinition.Property<Integer> getVersionProperty(Class<? extends CollectibleDocument> documentClass,
                                                                               String versionField,
                                                                               CollectibleDocumentDefinition definition) {
        CollectibleDocumentDefinition.Property<?> property = definition.getPropertyMap().get(versionField);
        if (property == null) {
            throw new DocumentClassDefinitionException("No property find:" + versionField + ", class:" + documentClass);
        }
        if (!int.class.equals(property.getType()) && !Integer.class.equals(property.getType())) {
            throw new DocumentClassDefinitionException("Version field not int type: " + versionField + ", class:" + documentClass);
        }
        return (CollectibleDocumentDefinition.Property<Integer>) property;
    }


    @Override
    public boolean casUpdateByVersion(CollectibleDocument document, String versionField) throws DocumentNoIdDjangoException {
        if (document.getId() == null) {
            throw new DocumentNoIdDjangoException("document no id:" + document.toString());
        }
        try {
            document.getSaveLock().lock();
            return doCasUpdateByVersion(document, versionField);
        } finally {
            document.getSaveLock().unlock();
        }
    }

    private boolean doCasUpdateByVersion(CollectibleDocument document, String versionField) {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(document.getClass());
        CollectibleDocumentDefinition.Property<Integer> versionProperty = getVersionProperty(document.getClass(),
                versionField, definition);
        int orgVersion = versionProperty.get(document);
        WriteContentDocument writeContent = getWriteContent(document, SaveMode.UPDATE_ONLY, true);
        //关闭UpdateRecord，因为后面可能要修改version字段，在此期间其他线程对文档做的修改在下次入库时会丢失
        //但由于调用cas方法时，document对象要单线程使用，所以不靠对文档对象的并发修改
        DocumentNodeHelper.disableUpdateRecord(document);
        if (writeContent == null) {
            writeContent = DefaultWriteContentDocument.createUpdate(new BsonDocument(), getCodecRegistry());
        }
        writeContent.put(versionField, orgVersion + 1);
        InternalSaveDocument internalSaveDocument = new InternalSaveDocument(document,
                MultiEquals.with(versionField, orgVersion).and(_id, document.getId()), writeContent, null);
        BulkWriteResult bulkWriteResult = null;
        try {
            bulkWriteResult = bulkSaveInternal(Collections.singletonList(internalSaveDocument), definition,
                    SaveMode.UPDATE_ONLY, false);
        } catch (Throwable throwable) {
            log.error("bulkSaveInternal failed. reset document record state.");
            throw throwable;
        }
        boolean success = bulkWriteResult.getMatchedCount() > 0;
        if (success) {
            versionProperty.set(document, orgVersion + 1);
            DocumentNodeHelper.enableUpdateRecord(document);
        }
        return success;
    }

    private String getDefaultVersionField(Class<? extends CollectibleDocument> documentClass) {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        String name = definition.getVersionFieldName();
        if (name == null) {
            throw new DocumentClassDefinitionException("No version field specified:" + documentClass);
        }
        return name;
    }

    @Override
    public boolean casUpdateMultiByVersion(List<CollectibleDocument> documentList) throws DocumentNoIdDjangoException {
        return casUpdateMultiByVersionNames(documentList.stream()
                .map(x -> new DocumentAndVersionField(x, getDefaultVersionField(x.getClass()))).collect(Collectors.toList()));
    }

    private static class CancelTransactionException extends RuntimeException {
    }

    @Override
    public boolean casUpdateMultiByVersionNames(List<DocumentAndVersionField> documentList)
            throws DocumentNoIdDjangoException {
        if (documentList.isEmpty()) {
            return true;
        }
        @AllArgsConstructor
        class Item {
            final CollectibleDocumentDefinition definition;
            final Bson filter;
            final Bson update;
        }
        int lockIndex = 0;
        try {
            List<Item> updates = new ArrayList<>(documentList.size());
            for (int i = 0; i < documentList.size(); i++) {
                DocumentAndVersionField dbDocumentAndVersionField = documentList.get(i);
                CollectibleDocument document = dbDocumentAndVersionField.getDocument();
                if (!document.getSaveLock().tryLock()) {
                    throw new ConcurrentSaveDjangoException();
                }
                CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(document.getClass());
                CollectibleDocumentDefinition.Property<Integer> versionProperty = getVersionProperty(document.getClass(),
                        dbDocumentAndVersionField.getVersionField(), definition);
                int orgVersion = versionProperty.get(document);
                WriteContentDocument writeContent = getWriteContent(document, SaveMode.UPDATE_ONLY, true);
                if (writeContent == null) {
                    writeContent = DefaultWriteContentDocument.createUpdate(new BsonDocument(), getCodecRegistry());
                }
                writeContent.put(dbDocumentAndVersionField.getVersionField(), orgVersion + 1);
                MultiEquals filter = MultiEquals.with(dbDocumentAndVersionField.getVersionField(), orgVersion).and(_id, document.getId());
                updates.add(new Item(definition, filter, writeContent.getAsBson()));
            }
            try {
                withNewSessionTransaction(dao -> {
                    for (Item item : updates) {
                        SessionBindingMongoCollection<? extends CollectibleDocument> collection = dao.getSessionBindingMongoDatabase().getCollection(
                                item.definition.getCollectionName(), item.definition.getDocumentClass());
                        UpdateResult result = collection.updateOne(item.filter, item.update);
                        if (result.getMatchedCount() == 0) {
                            throw new CancelTransactionException();
                        }
                    }
                    return null;
                }, null, null);
                return true;
            } catch (Throwable throwable) {
                for (DocumentAndVersionField dbDocumentAndVersionField : documentList) {
                    DocumentNodeHelper.disableUpdateRecord(dbDocumentAndVersionField.getDocument());
                }
                if (throwable instanceof CancelTransactionException) {
                    return false;
                } else {
                    throw throwable;
                }
            }
        } finally {
            for (int i = 0; i <= lockIndex; i++) {
                DocumentAndVersionField dbDocumentAndVersionField = documentList.get(i);
                dbDocumentAndVersionField.getDocument().getSaveLock().unlock();
            }
        }
    }

    @Override
    public MongoDatabase getMongoDatabase() {
        return database;
    }

    @Override
    public <T extends CollectibleDocument> MongoCollection<T> getMongoCollection(Class<T> documentClass) {
        CollectibleDocumentDefinition documentDefinition = CollectibleDocumentDefinitions.getDocumentDefinition(documentClass);
        return database.getCollection(documentDefinition.getCollectionName(), documentClass);
    }

    @Override
    public <T> T withNewSessionTransaction(TransactionRunner<T> transactionRunner,
                                           @Nullable ClientSessionOptions sessionOptions,
                                           @Nullable TransactionOptions transactionOptions) {
        DatabaseDaoWithSession dao = getDatabaseDaoFactory().createDaoWithSession(config, sessionOptions);
        try {
            if (transactionOptions == null) {
                transactionOptions = TransactionOptions.builder().build();
            }
            return dao.getSession().withTransaction(() -> {
                return transactionRunner.execute(dao);
            }, transactionOptions);
        } finally {
            try {
                dao.close();
            } catch (IOException e) {
                log.error("dao close", e);
            }
        }
    }
}
