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

import com.mountsea.django.core.annotation.KeyClass;
import com.mountsea.django.core.annotation.VersionField;
import com.mountsea.django.core.exception.IllegalKeyDjangoException;
import com.mountsea.django.core.util.AttributeMap;
import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.function.Function;

/**
 * {@link CollectibleDocument}的类信息，从 {@link CollectibleDocumentDefinitions}获取
 *
 * @author guch
 * @since 3.0.0
 */
public interface CollectibleDocumentDefinition {

    Class<? extends CollectibleDocument> getDocumentClass();

    /**
     * 对应的CollectionName。
     */
    String getCollectionName();

    Property<Object> getIdProperty();

    /**
     * 获取所有属性
     *
     * @return key 属性名
     */
    LinkedHashMap<String, Property<?>> getPropertyMap();

    /**
     * @see VersionField
     */
    @Nullable
    String getVersionFieldName();

    /**
     * 获取KeyDefinition
     */
    KeyDefinition getKeyDefinition();

    /**
     * @see com.mountsea.django.bson.annotation.FillNullByEmpty
     */
    boolean isFillNullByEmpty();

    /**
     * @see com.mountsea.django.core.annotation.AllowNullKeyField
     */
    boolean isAllowNullKeyField();

    /**
     * 获取用于存储动态初始化数据的AttributeMap
     */
    AttributeMap getAttributeMap();

    interface KeyDefinition {
        /**
         * 是否id为key
         */
        boolean isId();

        /**
         * 获取KeyClass，如果不是@KeyClass标注的，则返回null
         */
        @Nullable
        Class<?> getAnnotationKeyClass();

        KeyExtractor getKeyExtractor();

        BsonKeyConverter getBsonKeyConverter();

        /**
         * 获取key的所有属性，Property属于KeyClass，如果有keyClass，通过Property可以读写keyClass的字段。
         * 如果没有keyClass（只有单个key字段），则不能调用Property的get/set方法.
         * <p>
         * 此处id属性的名字为id，而不是_id
         *
         * @return key 属性名
         */
        LinkedHashMap<String, Property<?>> getPropertyMap();

        /**
         * @return 是否Key中只有一个字段，但这一个字段是一个结构类，本身包含了多个字段
         */
        boolean isSingleFiledAndMultiPropKey();

        /**
         * 如果 {@link #isSingleFiledAndMultiPropKey}为true，则可以获取此Map
         */
        LinkedHashMap<String, Property<?>> getSingleFieldMultiPropMap();
    }

    interface Property<T> {
        String getPropertyName();

        //如果是primitive类型，则返回对应的box类型
        Type getType();

        Class<?> getRawClass();

        void set(Object document, T fieldValue);

        T get(Object document);
    }

    /**
     * 从document获取key
     *
     * @author guch
     * @since 3.0.0
     */
    interface KeyExtractor {

        /**
         * 获取key，如果key只有一个字段，则返回的key为该字段类型。
         * 否则key是通过 {@link KeyClass}指定的，返回的key为该类型。
         *
         * @param document       document
         * @param allowNullField 是否允许key中存在null字段，如果为false，存在null字段时抛出 {@link IllegalKeyDjangoException}
         * @return key
         */
        Object extractKey(CollectibleDocument document, boolean allowNullField) throws IllegalKeyDjangoException;

        /**
         * 获取BsonDocument格式的key，无论key中有多少字段，格式都一样。
         *
         * @param document       document
         * @param allowNullField 是否允许key中存在null字段，如果为false，存在null字段时抛出 {@link IllegalKeyDjangoException}
         * @return key
         */
        BsonDocument extractBsonKey(CollectibleDocument document, boolean allowNullField) throws IllegalKeyDjangoException;
    }

    interface BsonKeyConverter {

        /**
         * 将key对象转换为bson filter
         */
        Bson keyToBsonFilter(Object key, CodecRegistry codecRegistry, boolean allowNullField);

        /**
         * 将key对象转换为BsonDocument，此BsonDocument也可以当做filter，但是创建BsonDocument成本更高
         */
        BsonDocument keyToBsonDocument(Object key, CodecRegistry codecRegistry, boolean allowNullField);
    }
}
