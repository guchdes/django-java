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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class DjangoFactoryManager {

    public final static DjangoFactoryManager INSTANCE = new DjangoFactoryManager();

    private final Map<String, DatabaseDaoFactory> daoFactoryMap = new HashMap<>(2);

    private final Map<String, MongoDataSource> mongoDataSourceMap = new HashMap<>(2);

    public void addDaoFactory(DatabaseDaoFactory daoFactory) {
        Objects.requireNonNull(daoFactory);
        Objects.requireNonNull(daoFactory.getName());
        MongoDataSource mongoDataSource = daoFactory.getMongoDataSource();
        if (!mongoDataSourceMap.containsKey(mongoDataSource.getName())) {
            addMongoDataSource(mongoDataSource);
        }
        DatabaseDaoFactory factory = daoFactoryMap.putIfAbsent(daoFactory.getName(), daoFactory);
        if (factory != null) {
            throw new IllegalStateException("DatabaseDaoFactory already exists :" + daoFactory.getName());
        }
    }

    public void addMongoDataSource(MongoDataSource mongoDataSource) {
        Objects.requireNonNull(mongoDataSource);
        Objects.requireNonNull(mongoDataSource.getName());
        MongoDataSource dataSource = mongoDataSourceMap.putIfAbsent(mongoDataSource.getName(), mongoDataSource);
        if (dataSource != null) {
            throw new IllegalStateException("MongoDataSource already exists :" + mongoDataSource.getName());
        }
    }

    @Nullable
    public DatabaseDaoFactory getDaoFactory(String name) {
        return daoFactoryMap.get(name);
    }

    @Nullable
    public MongoDataSource getMongoDataSource(String name) {
        return mongoDataSourceMap.get(name);
    }

}
