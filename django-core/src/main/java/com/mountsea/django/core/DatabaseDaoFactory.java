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
import com.mongodb.client.MongoDatabase;

import javax.annotation.Nullable;

/**
 * @author guch
 * @since 3.0.0
 */
public interface DatabaseDaoFactory {

    String GLOBAL_DAO_FACTORY_NAME = "global";

    String getName();

    DaoConfig getDefaultConfig();

    /**
     * 修改默认配置，并返回新的 DatabaseDaoFactory
     */
    DatabaseDaoFactory withDefaultConfig(DaoConfig config);

    /**
     * @return 创建DatabaseDao，使用当前factory的默认配置
     */
    DatabaseDao createDao();

    /**
     * @return 创建DatabaseDao，使用当前factory的默认配置，并指定database
     */
    DatabaseDao createDao(String database);

    /**
     * @return 创建DatabaseDao，使用指定的配置
     */
    DatabaseDao createDao(DaoConfig config);

    /**
     * @return 创建绑定session的DatabaseDao
     */
    DatabaseDaoWithSession createDaoWithSession(@Nullable ClientSessionOptions sessionOptions);

    /**
     * @return 创建绑定session的DatabaseDao
     */
    DatabaseDaoWithSession createDaoWithSession(String database, @Nullable ClientSessionOptions sessionOptions);

    /**
     * @return 创建绑定session的DatabaseDao
     */
    DatabaseDaoWithSession createDaoWithSession(DaoConfig config, @Nullable ClientSessionOptions sessionOptions);

    /**
     * 获取原生驱动的MongoDatabase
     */
    MongoDatabase getMongoDatabase(String database);

    MongoDataSource getMongoDataSource();
}
