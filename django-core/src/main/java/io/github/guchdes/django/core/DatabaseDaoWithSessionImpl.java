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
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @Author guch
 * @Since 3.0.0
 */
@Slf4j(topic = "io.github.guchdes.django.core.dao")
class DatabaseDaoWithSessionImpl extends DatabaseDaoImpl implements DatabaseDaoWithSession {

    public DatabaseDaoWithSessionImpl(MongoClient mongoClient, DaoConfig config, DatabaseDaoFactory databaseDaoFactory,
                                      @Nullable ClientSessionOptions sessionOptions) {
        super(mongoClient, config, databaseDaoFactory, sessionOptions);
    }

    public boolean isClosed() {
        return isSessionClosed;
    }

    @Override
    public ClientSession getSession() {
        return clientSession.get();
    }

    @Override
    public SessionBindingMongoDatabase getSessionBindingMongoDatabase() {
        return sessionBindingMongoDatabase;
    }

    @Override
    public void close() throws IOException {
        getSession().close();
    }

    @Override
    public <T> T withTransaction(TransactionRunner<T> transactionRunner) {
        return withTransaction(transactionRunner, null);
    }

    @Override
    public <T> T withTransaction(TransactionRunner<T> transactionRunner, @Nullable TransactionOptions transactionOptions) {
        if (transactionOptions == null) {
            transactionOptions = TransactionOptions.builder().build();
        }
        return getSession().withTransaction(() -> {
            return transactionRunner.execute(this);
        }, transactionOptions);
    }

    @Override
    public void withTransaction(Runnable transactionBody) {
        withTransaction(transactionBody, null);
    }

    @Override
    public void withTransaction(Runnable transactionBody, @Nullable TransactionOptions transactionOptions) {
        withTransaction((dao) -> {
            transactionBody.run();
            return null;
        }, transactionOptions);
    }
}
