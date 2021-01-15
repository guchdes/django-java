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

import com.mongodb.Block;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.connection.ServerDescription;
import com.mongodb.connection.ServerSettings;
import com.mongodb.lang.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for the acceptance tests.
 */
public final class Fixture {
    public static final String DEFAULT_URI = "mongodb://localhost:27017";
    public static final String DJANGO_URI_SYSTEM_PROPERTY_NAME = "django.test.uri";
    private static final String DEFAULT_DATABASE_NAME = "DjangoTest";
    private static final long MIN_HEARTBEAT_FREQUENCY_MS = 50L;

    private static MongoClient mongoClient;
    private static MongoClientSettings mongoClientSettings;
    private static MongoDatabase defaultDatabase;
    private static DjangoDaoFactory defaultDjangoDaoFactory;

    private Fixture() {
    }

    public static synchronized MongoClient getMongoClient() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(getMongoClientSettings());
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        }
        return mongoClient;
    }

    public static synchronized MongoDatabase getDefaultDatabase() {
        if (defaultDatabase == null) {
            defaultDatabase = getMongoClient().getDatabase(getDefaultDatabaseName());
        }
        return defaultDatabase;
    }

    public static String getDefaultDatabaseName() {
        return DEFAULT_DATABASE_NAME;
    }

    public static synchronized DjangoDaoFactory getDjangoDaoFactory() {
        if (defaultDjangoDaoFactory == null) {
            DaoConfig daoConfig = DaoConfig.builder().database(DEFAULT_DATABASE_NAME).build();
            DaoFactoryConfig daoFactoryConfig = DaoFactoryConfig.builder(DatabaseDaoFactory.GLOBAL_DAO_FACTORY_NAME)
                    .mongoDataSource(new SimpleMongoDataSource(getMongoClient()))
                    .defaultDaoConfig(daoConfig.toBuilder()).build();
            defaultDjangoDaoFactory = new DjangoDaoFactory(daoFactoryConfig);
        }
        return defaultDjangoDaoFactory;
    }

    static class ShutdownHook extends Thread {
        @Override
        public void run() {
            synchronized (Fixture.class) {
                if (mongoClient != null) {
                    if (defaultDatabase != null) {
                        defaultDatabase.drop();
                    }
                    mongoClient.close();
                    mongoClient = null;
                }
            }
        }
    }

    private static synchronized String getConnectionStringProperty() {
        return getConnectionString().getConnectionString();
    }

    public static synchronized ConnectionString getConnectionString() {
        ConnectionString mongoURIProperty = getConnectionStringFromSystemProperty(DJANGO_URI_SYSTEM_PROPERTY_NAME);
        if (mongoURIProperty != null) {
            return mongoURIProperty;
        }

        return new ConnectionString(DEFAULT_URI);
    }

    public static synchronized MongoClientSettings getMongoClientSettings() {
        if (mongoClientSettings == null) {
            String connectionStringProperty = getConnectionStringProperty();
            System.out.println("Using ConnectionString:" + connectionStringProperty);
            MongoClientSettings.Builder builder = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionStringProperty))
                    .applyToServerSettings(new Block<ServerSettings.Builder>() {
                        @Override
                        public void apply(final ServerSettings.Builder builder) {
                            builder.minHeartbeatFrequency(MIN_HEARTBEAT_FREQUENCY_MS, TimeUnit.MILLISECONDS);
                        }
                    });
            mongoClientSettings = builder.build();
        }
        return mongoClientSettings;
    }

    public static MongoClientSettings.Builder getMongoClientSettingsBuilder() {
        return MongoClientSettings.builder(getMongoClientSettings());
    }

    @SuppressWarnings("deprecation")
    public static ServerAddress getPrimary() throws InterruptedException {
        getMongoClient();
        List<ServerDescription> serverDescriptions = ((MongoClientImpl) mongoClient).getCluster().getDescription().getPrimaries();
        while (serverDescriptions.isEmpty()) {
            Thread.sleep(100);
            serverDescriptions = ((MongoClientImpl) mongoClient).getCluster().getDescription().getPrimaries();
        }
        return serverDescriptions.get(0).getAddress();
    }

    @Nullable
    private static ConnectionString getConnectionStringFromSystemProperty(final String property) {
        String mongoURIProperty = System.getProperty(property);
        if (mongoURIProperty == null) {
            mongoURIProperty = System.getenv(property);
        }
        if (mongoURIProperty != null && !mongoURIProperty.isEmpty()) {
            return new ConnectionString(mongoURIProperty);
        }
        return null;
    }

}
