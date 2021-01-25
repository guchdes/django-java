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
package com.mountsea.django.core.spring;

import com.mountsea.django.core.DatabaseDaoFactory;
import com.mountsea.django.core.MongoDataSource;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guch
 * @since 3.0.0
 */
@Data
public class DjangoProperties {

    /**
     * 全局MongoDataSource的名称为 global，DaoFactory未配置MongoDataSource时，默认使用global。
     */
    private Map<String, MongoDataSourceProperties> dataSources = new HashMap<>();

    /**
     * 全局DaoFactory的名称为 global，{@link DjangoInject}未指定factoryName时，默认使用global
     */
    private Map<String, DaoFactoryProperties> daoFactories = new HashMap<>();

    /**
     * daoFactory的默认database
     */
    private String defaultDatabase;

    @Data
    public static class DaoFactoryProperties {

        private String dataSourceName;

        private String database;

        // 开启DocumentDao缓存
        private boolean enableCache = true;

    }

    @Data
    public static class MongoDataSourceProperties {

        private String connectionString;

    }

}
