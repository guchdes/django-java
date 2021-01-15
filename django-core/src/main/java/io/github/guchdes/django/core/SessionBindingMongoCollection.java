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

import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.lang.Nullable;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.function.Supplier;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class SessionBindingMongoCollection<T> {
    private final com.mongodb.client.MongoCollection<T> mongoCollection;

    private final Supplier<ClientSession> clientSessionSupplier;

    public SessionBindingMongoCollection(com.mongodb.client.MongoCollection<T> mongoCollection, Supplier<ClientSession> clientSessionSupplier) {
        this.mongoCollection = mongoCollection;
        this.clientSessionSupplier = clientSessionSupplier;
    }

    public MongoNamespace getNamespace() {
        return mongoCollection.getNamespace();
    }


    public Class<T> getDocumentClass() {
        return mongoCollection.getDocumentClass();
    }


    public CodecRegistry getCodecRegistry() {
        return mongoCollection.getCodecRegistry();
    }


    public ReadPreference getReadPreference() {
        return mongoCollection.getReadPreference();
    }


    public WriteConcern getWriteConcern() {
        return mongoCollection.getWriteConcern();
    }


    public ReadConcern getReadConcern() {
        return mongoCollection.getReadConcern();
    }


    public <NewTDocument> com.mongodb.client.MongoCollection<NewTDocument> withDocumentClass(Class<NewTDocument> clazz) {
        return mongoCollection.withDocumentClass(clazz);
    }


    public com.mongodb.client.MongoCollection<T> withCodecRegistry(CodecRegistry codecRegistry) {
        return mongoCollection.withCodecRegistry(codecRegistry);
    }


    public com.mongodb.client.MongoCollection<T> withReadPreference(ReadPreference readPreference) {
        return mongoCollection.withReadPreference(readPreference);
    }


    public com.mongodb.client.MongoCollection<T> withWriteConcern(WriteConcern writeConcern) {
        return mongoCollection.withWriteConcern(writeConcern);
    }


    public com.mongodb.client.MongoCollection<T> withReadConcern(ReadConcern readConcern) {
        return mongoCollection.withReadConcern(readConcern);
    }


    @Deprecated
    public long count() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.count();
        } else {
            return mongoCollection.count(clientSession);
        }
    }

    @Deprecated
    public long count(Bson filter) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.count(filter);
        } else {
            return mongoCollection.count(clientSession, filter);
        }
    }

    @Deprecated
    public long count(Bson filter, CountOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.count(filter, options);
        } else {
            return mongoCollection.count(clientSession, filter, options);
        }
    }


    public long countDocuments() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.countDocuments();
        } else {
            return mongoCollection.countDocuments(clientSession);
        }
    }


    public long countDocuments(Bson filter) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.countDocuments(filter);
        } else {
            return mongoCollection.countDocuments(clientSession, filter);
        }
    }


    public long countDocuments(Bson filter, CountOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.countDocuments(filter, options);
        } else {
            return mongoCollection.countDocuments(clientSession, filter, options);
        }
    }


    public long estimatedDocumentCount() {
        return mongoCollection.estimatedDocumentCount();
    }


    public long estimatedDocumentCount(EstimatedDocumentCountOptions options) {
        return mongoCollection.estimatedDocumentCount(options);
    }


    public <TResult> DistinctIterable<TResult> distinct(String fieldName, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.distinct(fieldName, tResultClass);
        } else {
            return mongoCollection.distinct(clientSession, fieldName, tResultClass);
        }
    }


    public <TResult> DistinctIterable<TResult> distinct(String fieldName, Bson filter, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.distinct(fieldName, filter, tResultClass);
        } else {
            return mongoCollection.distinct(clientSession, fieldName, filter, tResultClass);
        }
    }


    public FindIterable<T> find() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.find();
        } else {
            return mongoCollection.find(clientSession);
        }
    }


    public <TResult> FindIterable<TResult> find(Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.find(tResultClass);
        } else {
            return mongoCollection.find(clientSession, tResultClass);
        }
    }


    public FindIterable<T> find(Bson filter) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.find(filter);
        } else {
            return mongoCollection.find(clientSession, filter);
        }
    }


    public <TResult> FindIterable<TResult> find(Bson filter, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.find(filter, tResultClass);
        } else {
            return mongoCollection.find(clientSession, filter, tResultClass);
        }
    }


    public AggregateIterable<T> aggregate(List<? extends Bson> pipeline) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.aggregate(pipeline);
        } else {
            return mongoCollection.aggregate(clientSession, pipeline);
        }
    }


    public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> pipeline, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.aggregate(pipeline, tResultClass);
        } else {
            return mongoCollection.aggregate(clientSession, pipeline, tResultClass);
        }
    }


    public ChangeStreamIterable<T> watch() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.watch();
        } else {
            return mongoCollection.watch(clientSession);
        }
    }


    public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.watch(tResultClass);
        } else {
            return mongoCollection.watch(clientSession, tResultClass);
        }
    }


    public ChangeStreamIterable<T> watch(List<? extends Bson> pipeline) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.watch(pipeline);
        } else {
            return mongoCollection.watch(clientSession, pipeline);
        }
    }


    public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.watch(pipeline, tResultClass);
        } else {
            return mongoCollection.watch(clientSession, pipeline, tResultClass);
        }
    }


    public MapReduceIterable<T> mapReduce(String mapFunction, String reduceFunction) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.mapReduce(mapFunction, reduceFunction);
        } else {
            return mongoCollection.mapReduce(clientSession, mapFunction, reduceFunction);
        }
    }


    public <TResult> MapReduceIterable<TResult> mapReduce(String mapFunction, String reduceFunction, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.mapReduce(mapFunction, reduceFunction, tResultClass);
        } else {
            return mongoCollection.mapReduce(clientSession, mapFunction, reduceFunction, tResultClass);
        }
    }


    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends T>> requests) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.bulkWrite(requests);
        } else {
            return mongoCollection.bulkWrite(clientSession, requests);
        }
    }


    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends T>> requests, BulkWriteOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.bulkWrite(requests, options);
        } else {
            return mongoCollection.bulkWrite(clientSession, requests, options);
        }
    }


    public void insertOne(T t) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.insertOne(t);
        } else {
            mongoCollection.insertOne(clientSession, t);
        }
    }


    public void insertOne(T t, InsertOneOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.insertOne(t, options);
        } else {
            mongoCollection.insertOne(clientSession, t, options);
        }
    }


    public void insertMany(List<? extends T> ts) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.insertMany(ts);
        } else {
            mongoCollection.insertMany(clientSession, ts);
        }
    }


    public void insertMany(List<? extends T> ts, InsertManyOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.insertMany(ts, options);
        } else {
            mongoCollection.insertMany(clientSession, ts, options);
        }
    }


    public DeleteResult deleteOne(Bson filter) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.deleteOne(filter);
        } else {
            return mongoCollection.deleteOne(clientSession, filter);
        }
    }


    public DeleteResult deleteOne(Bson filter, DeleteOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.deleteOne(filter, options);
        } else {
            return mongoCollection.deleteOne(clientSession, filter, options);
        }
    }


    public DeleteResult deleteMany(Bson filter) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.deleteMany(filter);
        } else {
            return mongoCollection.deleteMany(clientSession, filter);
        }
    }


    public DeleteResult deleteMany(Bson filter, DeleteOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.deleteMany(filter, options);
        } else {
            return mongoCollection.deleteMany(clientSession, filter, options);
        }
    }


    public UpdateResult replaceOne(Bson filter, T replacement) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.replaceOne(filter, replacement);
        } else {
            return mongoCollection.replaceOne(clientSession, filter, replacement);
        }
    }


    @Deprecated
    public UpdateResult replaceOne(Bson filter, T replacement, UpdateOptions updateOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.replaceOne(filter, replacement, updateOptions);
        } else {
            return mongoCollection.replaceOne(clientSession, filter, replacement, updateOptions);
        }
    }


    public UpdateResult replaceOne(Bson filter, T replacement, ReplaceOptions replaceOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.replaceOne(filter, replacement, replaceOptions);
        } else {
            return mongoCollection.replaceOne(clientSession, filter, replacement, replaceOptions);
        }
    }


    public UpdateResult updateOne(Bson filter, Bson update) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.updateOne(filter, update);
        } else {
            return mongoCollection.updateOne(clientSession, filter, update);
        }
    }


    public UpdateResult updateOne(Bson filter, Bson update, UpdateOptions updateOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.updateOne(filter, update, updateOptions);
        } else {
            return mongoCollection.updateOne(clientSession, filter, update, updateOptions);
        }
    }


    public UpdateResult updateOne(Bson filter, List<? extends Bson> update) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.updateOne(filter, update);
        } else {
            return mongoCollection.updateOne(clientSession, filter, update);
        }
    }


    public UpdateResult updateOne(Bson filter, List<? extends Bson> update, UpdateOptions updateOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.updateOne(filter, update, updateOptions);
        } else {
            return mongoCollection.updateOne(clientSession, filter, update, updateOptions);
        }
    }


    public UpdateResult updateMany(Bson filter, Bson update) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.updateMany(filter, update);
        } else {
            return mongoCollection.updateMany(clientSession, filter, update);
        }
    }


    public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.updateMany(filter, update, updateOptions);
        } else {
            return mongoCollection.updateMany(clientSession, filter, update, updateOptions);
        }
    }


    public UpdateResult updateMany(Bson filter, List<? extends Bson> update) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.updateMany(filter, update);
        } else {
            return mongoCollection.updateMany(clientSession, filter, update);
        }
    }


    public UpdateResult updateMany(Bson filter, List<? extends Bson> update, UpdateOptions updateOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.updateMany(filter, update, updateOptions);
        } else {
            return mongoCollection.updateMany(clientSession, filter, update, updateOptions);
        }
    }


    @Nullable
    public T findOneAndDelete(Bson filter) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.findOneAndDelete(filter);
        } else {
            return mongoCollection.findOneAndDelete(clientSession, filter);
        }
    }


    @Nullable
    public T findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.findOneAndDelete(filter, options);
        } else {
            return mongoCollection.findOneAndDelete(clientSession, filter, options);
        }
    }


    @Nullable
    public T findOneAndReplace(Bson filter, T replacement) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.findOneAndReplace(filter, replacement);
        } else {
            return mongoCollection.findOneAndReplace(clientSession, filter, replacement);
        }
    }


    @Nullable
    public T findOneAndReplace(Bson filter, T replacement, FindOneAndReplaceOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.findOneAndReplace(filter, replacement, options);
        } else {
            return mongoCollection.findOneAndReplace(clientSession, filter, replacement, options);
        }
    }


    @Nullable
    public T findOneAndUpdate(Bson filter, Bson update) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.findOneAndUpdate(filter, update);
        } else {
            return mongoCollection.findOneAndUpdate(clientSession, filter, update);
        }
    }


    @Nullable
    public T findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.findOneAndUpdate(filter, update, options);
        } else {
            return mongoCollection.findOneAndUpdate(clientSession, filter, update, options);
        }
    }


    @Nullable
    public T findOneAndUpdate(Bson filter, List<? extends Bson> update) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.findOneAndUpdate(filter, update);
        } else {
            return mongoCollection.findOneAndUpdate(clientSession, filter, update);
        }
    }


    @Nullable
    public T findOneAndUpdate(Bson filter, List<? extends Bson> update, FindOneAndUpdateOptions options) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.findOneAndUpdate(filter, update, options);
        } else {
            return mongoCollection.findOneAndUpdate(clientSession, filter, update, options);
        }
    }


    public void drop() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.drop();
        } else {
            mongoCollection.drop(clientSession);
        }
    }


    public String createIndex(Bson keys) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.createIndex(keys);
        } else {
            return mongoCollection.createIndex(clientSession, keys);
        }
    }


    public String createIndex(Bson keys, IndexOptions indexOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.createIndex(keys, indexOptions);
        } else {
            return mongoCollection.createIndex(clientSession, keys, indexOptions);
        }
    }


    public List<String> createIndexes(List<IndexModel> indexes) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.createIndexes(indexes);
        } else {
            return mongoCollection.createIndexes(clientSession, indexes);
        }
    }


    public List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.createIndexes(indexes, createIndexOptions);
        } else {
            return mongoCollection.createIndexes(clientSession, indexes, createIndexOptions);
        }
    }


    public ListIndexesIterable<Document> listIndexes() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.listIndexes();
        } else {
            return mongoCollection.listIndexes(clientSession);
        }
    }


    public <TResult> ListIndexesIterable<TResult> listIndexes(Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return mongoCollection.listIndexes(tResultClass);
        } else {
            return mongoCollection.listIndexes(clientSession, tResultClass);
        }
    }


    public void dropIndex(String indexName) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.dropIndex(indexName);
        } else {
            mongoCollection.dropIndex(clientSession, indexName);
        }
    }


    public void dropIndex(String indexName, DropIndexOptions dropIndexOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.dropIndex(indexName, dropIndexOptions);
        } else {
            mongoCollection.dropIndex(clientSession, indexName, dropIndexOptions);
        }
    }


    public void dropIndex(Bson keys) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.dropIndex(keys);
        } else {
            mongoCollection.dropIndex(clientSession, keys);
        }
    }


    public void dropIndex(Bson keys, DropIndexOptions dropIndexOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.dropIndex(keys, dropIndexOptions);
        } else {
            mongoCollection.dropIndex(clientSession, keys, dropIndexOptions);
        }
    }


    public void dropIndexes() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.dropIndexes();
        } else {
            mongoCollection.dropIndexes(clientSession);
        }
    }


    public void dropIndexes(DropIndexOptions dropIndexOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.dropIndexes(dropIndexOptions);
        } else {
            mongoCollection.dropIndexes(clientSession, dropIndexOptions);
        }
    }


    public void renameCollection(MongoNamespace newCollectionNamespace) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.renameCollection(newCollectionNamespace);
        } else {
            mongoCollection.renameCollection(clientSession, newCollectionNamespace);
        }
    }


    public void renameCollection(MongoNamespace newCollectionNamespace, RenameCollectionOptions renameCollectionOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            mongoCollection.renameCollection(newCollectionNamespace, renameCollectionOptions);
        } else {
            mongoCollection.renameCollection(clientSession, newCollectionNamespace, renameCollectionOptions);
        }
    }
}
