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
import com.mongodb.client.MongoDatabase;

import javax.annotation.Nullable;

/**
 * @author guch
 * @since 3.0.0
 */
public class DjangoDaoFactory implements DatabaseDaoFactory {

    private final DaoFactoryConfig config;

    public DjangoDaoFactory(DaoFactoryConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return config.getFactoryName();
    }

    @Override
    public MongoDataSource getMongoDataSource() {
        return config.getMongoDataSource();
    }

    @Override
    public DaoConfig getDefaultConfig() {
        return config.getDefaultDaoConfig();
    }

    @Override
    public DatabaseDaoFactory withDefaultConfig(DaoConfig daoConfig) {
        DaoFactoryConfig build = config.toBuilder().defaultDaoConfig(daoConfig.toBuilder()).build();
        return new DjangoDaoFactory(build);
    }

    @Override
    public DatabaseDao createDao() {
        return createDao(config.getDefaultDaoConfig());
    }

    @Override
    public DatabaseDao createDao(String database) {
        return createDao(config.getDefaultDaoConfig().toBuilder().database(database).build());
    }

    @Override
    public DatabaseDao createDao(DaoConfig daoConfig) {
        return new DatabaseDaoImpl(config.getMongoDataSource().getMongoClient(),
                daoConfig, this, null);
    }

    @Override
    public DatabaseDaoWithSession createDaoWithSession(@Nullable ClientSessionOptions sessionOptions) {
        return createDaoWithSession(config.getDefaultDaoConfig(), sessionOptions);
    }

    @Override
    public DatabaseDaoWithSession createDaoWithSession(String database, @Nullable ClientSessionOptions sessionOptions) {
        return createDaoWithSession(config.getDefaultDaoConfig().toBuilder().database(database).build(), sessionOptions);
    }

    @Override
    public DatabaseDaoWithSession createDaoWithSession(DaoConfig daoConfig, @Nullable ClientSessionOptions sessionOptions) {
        if (sessionOptions == null) {
            sessionOptions = ClientSessionOptions.builder().build();
        }
        return new DatabaseDaoWithSessionImpl(config.getMongoDataSource().getMongoClient(),
                daoConfig, this, sessionOptions);
    }

    @Override
    public MongoDatabase getMongoDatabase(String s) {
        return config.getMongoDataSource().getMongoClient().getDatabase(s);
    }
}
