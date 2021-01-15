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
package io.github.guchdes.django.core.spring;

import io.github.guchdes.django.core.*;
import io.github.guchdes.django.core.cache.CachePlugin;
import io.github.guchdes.django.core.cache.DocumentCacheProperties;
import io.github.guchdes.django.core.spring.DjangoProperties.MongoDataSourceProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Map;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class DjangoAnnotationInjectionProcessor extends AnnotationInjectionProcessor implements Ordered {

    final static int ORDER = Ordered.LOWEST_PRECEDENCE;

    @Autowired
    private ApplicationContext applicationContext;

    private boolean hasProcessDjangoFactoryManager = false;

    public DjangoAnnotationInjectionProcessor() {
        add(DjangoInject.class, aClass -> {
            return aClass.isAssignableFrom(DatabaseDao.class) || aClass.equals(DatabaseDaoFactory.class);
        }, this::provideDjangoObject);
    }

    private synchronized DjangoFactoryManager getDjangoFactoryManager() {
        if (!hasProcessDjangoFactoryManager) {
            DjangoProperties djangoProperties = applicationContext.getBean(DjangoProperties.class);
            MongoDataSourceConfigurer mongoDataSourceConfigurer = combine(
                    applicationContext.getBeansOfType(MongoDataSourceConfigurer.class).values());
            DaoFactoryConfigurer daoFactoryConfigurers = combine2(
                    applicationContext.getBeansOfType(DaoFactoryConfigurer.class).values());

            ObjectProvider<CachePlugin> cachePlugins = applicationContext.getBeanProvider(CachePlugin.class);
            ObjectProvider<DocumentCacheProperties> documentCacheProperties = applicationContext.getBeanProvider(DocumentCacheProperties.class);

            Map<String, MongoDataSourceProperties> dataSource = djangoProperties.getDataSources();
            for (Map.Entry<String, MongoDataSourceProperties> entry : dataSource.entrySet()) {
                MongoDataSourceProperties properties = entry.getValue();
                MongoDataSourceConfig mongoDataSourceConfig = MongoDataSourceConfig.builder()
                        .name(entry.getKey())
                        .connectionString(properties.getConnectionString())
                        .mongoDataSourceConfigurer(mongoDataSourceConfigurer)
                        .build();
                DjangoFactoryManager.INSTANCE.addMongoDataSource(new ConfigMongoDataSource(mongoDataSourceConfig));
            }

            Map<String, DjangoProperties.DaoFactoryProperties> daoFactory = djangoProperties.getDaoFactories();
            for (Map.Entry<String, DjangoProperties.DaoFactoryProperties> entry : daoFactory.entrySet()) {
                DaoFactoryConfig.Builder factoryBuilder = DaoFactoryConfig.builder(entry.getKey());
                DjangoProperties.DaoFactoryProperties properties = entry.getValue();
                String dataSourceName = properties.getDataSourceName();
                MongoDataSource mongoDataSource = DjangoFactoryManager.INSTANCE.getMongoDataSource(
                        (dataSourceName == null || dataSourceName.isEmpty()) ? MongoDataSource.GLOBAL_DATA_SOURCE_NAME : dataSourceName);
                DaoConfig.Builder builder = DaoConfig.builder();
                factoryBuilder.mongoDataSource(mongoDataSource);
                factoryBuilder.documentCacheConfigs(documentCacheProperties.getIfUnique());
                builder.database(StringUtils.isBlank(properties.getDatabase()) ?
                        djangoProperties.getDefaultDatabase() : properties.getDatabase());
                builder.isCacheEnable(properties.isEnableCache());
                if (cachePlugins.getIfUnique() != null) {
                    builder.cachePlugin(cachePlugins.getIfUnique());
                }
                factoryBuilder.defaultDaoConfig(builder);
                daoFactoryConfigurers.accept(factoryBuilder);
                DjangoFactoryManager.INSTANCE.addDaoFactory(new DjangoDaoFactory(factoryBuilder.build()));
            }
            hasProcessDjangoFactoryManager = true;
        }
        return DjangoFactoryManager.INSTANCE;
    }

    private Object provideDjangoObject(Object bean, AnnotatedElement annotatedElement, Class<?> targetClass,
                                       DjangoInject annotation) {
        String factoryName = annotation.factory();
        DatabaseDaoFactory daoFactory = getDaoFactory(factoryName);
        DaoConfig.Builder daoConfigBuilder = daoFactory.getDefaultConfig().toBuilder();
        if (!annotation.enableCache().isDefault()) {
            daoConfigBuilder.isCacheEnable(annotation.enableCache().toBool());
        }
        if (targetClass.isAssignableFrom(DatabaseDao.class)) {
            DaoConfig config = daoConfigBuilder.build();
            if (config.getDatabase() == null) {
                throw new BeanInitializationException("Not default database config for daoFactory:" + factoryName);
            }
            return daoFactory.createDao(config);
        } else {
            return daoFactory.withDefaultConfig(daoConfigBuilder.build());
        }
    }

    private DatabaseDaoFactory getDaoFactory(String name) {
        DatabaseDaoFactory daoFactory = getDjangoFactoryManager().getDaoFactory(name);
        if (daoFactory == null) {
            throw new BeanInitializationException("Not found daoFactory:" + name);
        }
        return daoFactory;
    }

    private DaoFactoryConfigurer combine2(Collection<DaoFactoryConfigurer> collection) {
        return (builder) -> {
            for (DaoFactoryConfigurer configurer : collection) {
                configurer.accept(builder);
            }
        };
    }

    private MongoDataSourceConfigurer combine(Collection<MongoDataSourceConfigurer> collection) {
        return (name, builder) -> {
            for (MongoDataSourceConfigurer configurer : collection) {
                configurer.accept(name, builder);
            }
        };
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
