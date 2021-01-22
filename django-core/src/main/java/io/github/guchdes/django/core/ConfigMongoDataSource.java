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

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.guchdes.django.bson.util.LazyInitializer;
import lombok.Getter;

/**
 * @author guch
 * @since 3.0.0
 */
public class ConfigMongoDataSource implements MongoDataSource {

    @Getter
    private final MongoDataSourceConfig config;

    private final LazyInitializer<MongoClient> mongoClient = new LazyInitializer<>(this::initMongoClient);

    public ConfigMongoDataSource(MongoDataSourceConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return config.getName();
    }

    public MongoClient getMongoClient() {
        return mongoClient.get();
    }

    public boolean isInitialized() {
        return mongoClient.isInitialized();
    }

    private MongoClient initMongoClient() {
        ConnectionString connectionString = new ConnectionString(config.getConnectionString());
        MongoClientSettings.Builder builder = MongoClientSettings.builder().applyConnectionString(connectionString);
        MongoDataSourceConfigurer configurer = config.getMongoDataSourceConfigurer();
        if (configurer != null) {
            configurer.accept(getName(), builder);
        }
        return MongoClients.create(builder.build());
    }
}
