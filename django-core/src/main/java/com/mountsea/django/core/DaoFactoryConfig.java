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

import com.mountsea.django.core.cache.DocumentCacheConfigs;
import com.mountsea.django.core.cache.DocumentCacheProperties;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * DaoFactory配置，用于创建 {@link DatabaseDaoFactory}
 *
 * @author guch
 * @since 3.0.0
 */
@Getter
@ToString
public class DaoFactoryConfig {

    private final String factoryName;

    private final DaoConfig defaultDaoConfig;

    private final MongoDataSource mongoDataSource;

    private final DocumentCacheConfigs documentCacheConfigs;

    public static Builder builder(String factoryName) {
        return new Builder(factoryName);
    }

    private DaoFactoryConfig(String factoryName, DaoConfig defaultDaoConfig,
                             MongoDataSource mongoDataSource, DocumentCacheConfigs documentCacheConfigs) {
        this.factoryName = factoryName;
        this.defaultDaoConfig = defaultDaoConfig;
        this.mongoDataSource = mongoDataSource;
        this.documentCacheConfigs = documentCacheConfigs;
    }

    public Builder toBuilder() {
        return new Builder(factoryName).defaultDaoConfig(defaultDaoConfig.toBuilder())
                .mongoDataSource(mongoDataSource).documentCacheConfigs(documentCacheConfigs);
    }

    @Getter
    public static class Builder {
        private final String factoryName;

        private Builder(String factoryName) {
            Objects.requireNonNull(factoryName);
            this.factoryName = factoryName;
        }

        private DaoConfig.Builder defaultDaoConfig = new DaoConfig.Builder();

        private MongoDataSource mongoDataSource;

        private DocumentCacheConfigs documentCacheConfigs;

        public Builder defaultDaoConfig(DaoConfig.Builder defaultDaoConfig) {
            this.defaultDaoConfig = defaultDaoConfig;
            return this;
        }

        public Builder mongoDataSource(MongoDataSource mongoDataSource) {
            this.mongoDataSource = mongoDataSource;
            return this;
        }

        public Builder documentCacheConfigs(DocumentCacheConfigs documentCacheConfigs) {
            this.documentCacheConfigs = documentCacheConfigs;
            return this;
        }

        public DaoFactoryConfig build() {
            if (mongoDataSource == null || defaultDaoConfig == null) {
                throw new NullPointerException("mongoDataSource == null || defaultDaoConfig == null");
            }
            DocumentCacheConfigs documentCacheConfigs = this.documentCacheConfigs;
            if (documentCacheConfigs == null) {
                documentCacheConfigs = new DocumentCacheProperties();
            }
            return new DaoFactoryConfig(factoryName, defaultDaoConfig.build(), mongoDataSource, documentCacheConfigs);
        }
    }
}
