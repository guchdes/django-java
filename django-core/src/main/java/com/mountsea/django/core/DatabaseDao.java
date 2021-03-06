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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import javax.annotation.Nullable;

/**
 * 主要的数据库操作接口。
 *
 * @author guch
 * @since 3.0.0
 */
public interface DatabaseDao extends DatabaseMetaDao, KeyOperationalDao, CollectionOperationalDao, ConcurrentOperationalDao {

    MongoDatabase getMongoDatabase();

    <T extends CollectibleDocument> MongoCollection<T> getMongoCollection(Class<T> documentClass);

    /**
     * 从DaoFactory创建绑定session的dao，执行事务，如果transactionBody正常执行则提交事务，抛出异常则放弃事务，最后关闭session。
     *
     * @param transactionRunner 事务中要执行的代码，注意在事务代码中要使用 {@link TransactionRunner#execute}方法参数的dao，
     *                          而不是当前dao
     */
    <T> T withNewSessionTransaction(TransactionRunner<T> transactionRunner,
                                    @Nullable ClientSessionOptions sessionOptions, @Nullable TransactionOptions transactionOptions);


}
