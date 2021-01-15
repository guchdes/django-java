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
package io.github.guchdes.django.core.spring;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.guchdes.django.core.DjangoFactoryManager;
import io.github.guchdes.django.core.cache.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author guch
 * @Since 3.0.0
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(DjangoMarkerConfiguration.DjangoMarker.class)
@Import({DjangoAnnotationInjectionProcessor.class})
public class DjangoAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    @ConfigurationProperties("django")
    public DjangoProperties djangoProperties() {
        return new DjangoProperties();
    }

    @Bean
    public DjangoFactoryManager djangoFactoryManager() {
        return DjangoFactoryManager.INSTANCE;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties("django.cache")
    public DocumentCacheProperties documentCacheProperties() {
        return new DocumentCacheProperties();
    }

    @ConditionalOnMissingBean(CachePlugin.class)
    @ConditionalOnClass(Caffeine.class)
    public static class CaffineCaches {
        @Bean
        public CachePlugin defaultCachePlugin(DocumentCacheProperties properties) {
            return new CaffineCachePlugin(properties);
        }
    }

}
