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

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.*;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateViewOptions;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * SessionBindingMongoDatabase代理了MongoDatabase的所有方法，
 * 创建SessionBindingMongoDatabase时可以传递一个SessionSupplier，每次调用方法时，尝试获取session，如果Supplier返回不为null
 * 则使用session。
 *
 * @author guch
 * @since 3.0.0
 */
public class SessionBindingMongoDatabase {
    private final com.mongodb.client.MongoDatabase delegated;

    private final Supplier<ClientSession> clientSessionSupplier;

    public SessionBindingMongoDatabase(MongoDatabase delegated, Supplier<ClientSession> clientSessionSupplier) {
        this(delegated, thisRef -> clientSessionSupplier);
    }

    /**
     * @param clientSessionSupplier 使用Function是为了在子类调用super构造方法时，能够访问到父类引用
     */
    public SessionBindingMongoDatabase(MongoDatabase delegated,
                                       Function<SessionBindingMongoDatabase, Supplier<ClientSession>> clientSessionSupplier) {
        this.delegated = delegated;
        this.clientSessionSupplier = clientSessionSupplier.apply(this);
    }

    public String getName() {
        return delegated.getName();
    }


    public CodecRegistry getCodecRegistry() {
        return delegated.getCodecRegistry();
    }


    public ReadPreference getReadPreference() {
        return delegated.getReadPreference();
    }


    public WriteConcern getWriteConcern() {
        return delegated.getWriteConcern();
    }


    public ReadConcern getReadConcern() {
        return delegated.getReadConcern();
    }


    public com.mongodb.client.MongoDatabase withCodecRegistry(CodecRegistry codecRegistry) {
        return delegated.withCodecRegistry(codecRegistry);
    }


    public com.mongodb.client.MongoDatabase withReadPreference(ReadPreference readPreference) {
        return delegated.withReadPreference(readPreference);
    }


    public com.mongodb.client.MongoDatabase withWriteConcern(WriteConcern writeConcern) {
        return delegated.withWriteConcern(writeConcern);
    }


    public com.mongodb.client.MongoDatabase withReadConcern(ReadConcern readConcern) {
        return delegated.withReadConcern(readConcern);
    }


    public SessionBindingMongoCollection<Document> getCollection(String collectionName) {
        return new SessionBindingMongoCollection<>(delegated.getCollection(collectionName), clientSessionSupplier);
    }


    public <TDocument> SessionBindingMongoCollection<TDocument> getCollection(String collectionName, Class<TDocument> tDocumentClass) {
        return new SessionBindingMongoCollection<>(delegated.getCollection(collectionName, tDocumentClass), clientSessionSupplier);
    }


    public Document runCommand(Bson command) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.runCommand(command);
        } else {
            return delegated.runCommand(clientSession, command);
        }
    }


    public Document runCommand(Bson command, ReadPreference readPreference) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.runCommand(command, readPreference);
        } else {
            return delegated.runCommand(clientSession, command, readPreference);
        }
    }


    public <TResult> TResult runCommand(Bson command, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.runCommand(command, tResultClass);
        } else {
            return delegated.runCommand(clientSession, command, tResultClass);
        }
    }


    public <TResult> TResult runCommand(Bson command, ReadPreference readPreference, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.runCommand(command, readPreference, tResultClass);
        } else {
            return delegated.runCommand(clientSession, command, readPreference, tResultClass);
        }
    }


    public void drop() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            delegated.drop();
        } else {
            delegated.drop(clientSession);
        }
    }


    public MongoIterable<String> listCollectionNames() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.listCollectionNames();
        } else {
            return delegated.listCollectionNames(clientSession);
        }
    }


    public ListCollectionsIterable<Document> listCollections() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.listCollections();
        } else {
            return delegated.listCollections(clientSession);
        }
    }


    public <TResult> ListCollectionsIterable<TResult> listCollections(Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.listCollections(tResultClass);
        } else {
            return delegated.listCollections(clientSession, tResultClass);
        }
    }


    public void createCollection(String collectionName) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            delegated.createCollection(collectionName);
        } else {
            delegated.createCollection(clientSession, collectionName);
        }
    }


    public void createCollection(String collectionName, CreateCollectionOptions createCollectionOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            delegated.createCollection(collectionName, createCollectionOptions);
        } else {
            delegated.createCollection(clientSession, collectionName, createCollectionOptions);
        }
    }


    public void createView(String viewName, String viewOn, List<? extends Bson> pipeline) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            delegated.createView(viewName, viewOn, pipeline);
        } else {
            delegated.createView(clientSession, viewName, viewOn, pipeline);
        }
    }


    public void createView(String viewName, String viewOn, List<? extends Bson> pipeline, CreateViewOptions createViewOptions) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            delegated.createView(viewName, viewOn, pipeline, createViewOptions);
        } else {
            delegated.createView(clientSession, viewName, viewOn, pipeline, createViewOptions);
        }
    }


    public ChangeStreamIterable<Document> watch() {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.watch();
        } else {
            return delegated.watch(clientSession);
        }
    }


    public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.watch(tResultClass);
        } else {
            return delegated.watch(clientSession, tResultClass);
        }
    }


    public ChangeStreamIterable<Document> watch(List<? extends Bson> pipeline) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.watch(pipeline);
        } else {
            return delegated.watch(clientSession, pipeline);
        }
    }


    public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.watch(pipeline, tResultClass);
        } else {
            return delegated.watch(clientSession, pipeline, tResultClass);
        }
    }


    public AggregateIterable<Document> aggregate(List<? extends Bson> pipeline) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.aggregate(pipeline);
        } else {
            return delegated.aggregate(clientSession, pipeline);
        }
    }


    public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> pipeline, Class<TResult> tResultClass) {
        ClientSession clientSession = clientSessionSupplier.get();
        if (clientSession == null) {
            return delegated.aggregate(pipeline, tResultClass);
        } else {
            return delegated.aggregate(clientSession, pipeline, tResultClass);
        }
    }
}
