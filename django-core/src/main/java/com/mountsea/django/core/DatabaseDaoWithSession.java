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

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;

import javax.annotation.Nullable;
import java.io.Closeable;

/**
 * 和session一对一绑定的DatabaseDao，有和session一样的生命周期，在创建dao时同时创建session，session关闭后dao不再可用.
 * <p>
 * 调用此dao的所有CRUD方法时，都会使用绑定的session。
 *
 * @author guch
 * @since 3.0.0
 */
public interface DatabaseDaoWithSession extends Closeable, DatabaseMetaDao, KeyOperationalDao,
        CollectionOperationalDao, ConcurrentOperationalDao {

    ClientSession getSession();

    /**
     * 获取SessionBindingMongoDatabase，通过SessionBindingMongoDatabase可以调用原生驱动的方法，
     * 同时它也绑定了当前dao中的session。当前dao关闭后，SessionBindingMongoDatabase也不再可用。
     */
    SessionBindingMongoDatabase getSessionBindingMongoDatabase();

    /**
     * 获取SessionBindingMongoCollection，通过SessionBindingMongoCollection可以调用原生驱动的方法，
     * 同时它也绑定了当前dao中的session。当前dao关闭后，SessionBindingMongoCollection也不再可用。
     */
    <T extends CollectibleDocument> SessionBindingMongoCollection<T> getSessionBindingMongoCollection(Class<T> tClass);

    /**
     * transactionRunner调用当前dao的方法，即可在事务中执行
     */
    <T> T withTransaction(TransactionRunner<T> transactionRunner);

    /**
     * transactionRunner调用当前dao的方法，即可在事务中执行
     */
    void withTransaction(Runnable transactionRunner);

    /**
     * transactionRunner调用当前dao的方法，即可在事务中执行
     */
    <T> T withTransaction(TransactionRunner<T> transactionRunner, @Nullable TransactionOptions transactionOptions);

    /**
     * transactionRunner调用当前dao的方法，即可在事务中执行
     */
    void withTransaction(Runnable transactionRunner, @Nullable TransactionOptions transactionOptions);

    boolean isClosed();
}
