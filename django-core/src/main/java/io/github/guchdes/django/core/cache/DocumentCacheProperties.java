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
package io.github.guchdes.django.core.cache;

import io.github.guchdes.django.core.CollectibleDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author guch
 * @Since 3.0.0
 */
@Getter
@Setter
public class DocumentCacheProperties implements DocumentCacheConfigs {

    /**
     * 所有类的默认缓存配置，可以被每个类的具体配置覆盖
     */
    private SingleItemCacheConfig defaultConfig;

    /**
     * 对每个类的缓存配置，key 类名
     */
    private Map<String, SingleItemCacheConfig> classes = new HashMap<>();

    @Override
    public SingleItemCacheConfig getDefaultConfig() {
        if (defaultConfig == null) {
            return DocumentCacheConfigs.DEFAULT_DEFAULT_CONFIG;
        }
        return defaultConfig;
    }

    @Override
    public SingleItemCacheConfig getDocumentClassConfig(Class<? extends CollectibleDocument> aClass) {
        return classes.get(aClass.getName());
    }


}
