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
package io.github.guchdes.django.bson.projection.pojo;

import io.github.guchdes.django.bson.KnownTypes;
import io.github.guchdes.django.bson.annotation.ImmutableDocument;
import io.github.guchdes.django.bson.projection.*;
import io.github.guchdes.django.bson.projection.dtbson.DtBsonValue;
import io.github.guchdes.django.bson.util.InternalUtils;
import org.apache.commons.lang3.ClassUtils;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.guchdes.django.bson.projection.pojo.ClassModelBuilder.ID_PROPERTY_NAME;
import static java.lang.String.format;

class GlobalModelsImpl {

    static final GlobalModelsImpl INSTANCE = new GlobalModelsImpl();

    private static final Map<Class<?>, ClassModel<?>> CLASS_MODEL_MAP = new ConcurrentHashMap<>();

    private static final DocumentNodeFillerProvider DOCUMENT_NODE_FILLER_PROVIDER = new DocumentNodeFillerProviderImpl();

    private static final List<Convention> ALL_CLASS_MODEL_CONVENTION_LIST = new CopyOnWriteArrayList<>(Conventions.DEFAULT_CONVENTIONS);

    private static final Map<Class<?>, Boolean> SIMPLE_TYPES = new ConcurrentHashMap<>();

    private static final ProxiedDocumentCreatorProvider DEFAULT_ENHANCER_PROVIDER = new DefaultProxiedDocumentCreatorProvider();

    /**
     * 创建ClassModel，不使用缓存
     */
    <T> ClassModel<T> createClassModel(Class<T> aClass) {
        ClassModelBuilder<T> builder = ClassModel.builder(aClass);
        builder.conventions(ALL_CLASS_MODEL_CONVENTION_LIST);
        return builder.build();
    }

    /**
     * 获取ClassModel，不存在则创建，并缓存结果
     */
    @SuppressWarnings("unchecked")
    <T> ClassModel<T> getClassModel(Class<T> aClass) {
        return (ClassModel<T>) InternalUtils.mapComputeIfAbsent(CLASS_MODEL_MAP, aClass, this::createClassModel);
    }

    <T extends DocumentNode> DocumentNodeFiller<T> getDocumentNodeFiller(Class<T> tClass) {
        return DOCUMENT_NODE_FILLER_PROVIDER.getDocumentNodeFiller(tClass);
    }

    /**
     * @return 一个类是否含有多个字段
     */
    boolean isSimpleType(Class<?> aClass) {
        if (ClassUtils.isPrimitiveOrWrapper(aClass)) {
            return true;
        }
        if (KnownTypes.isSimpleAndImmutableType(aClass)) {
            return true;
        }
        if (isContainerType(aClass)) {
            return false;
        }
        return InternalUtils.mapComputeIfAbsent(SIMPLE_TYPES, aClass, aClass1 -> {
            List<PropertyModel<?>> propertyModels = createPropertyModels(aClass1);
            int readables = 0;
            for (PropertyModel<?> propertyModel : propertyModels) {
                if (propertyModel.isReadable() && propertyModel.getPropertyMetadata().isSerializable()) {
                    readables++;
                }
            }
            return readables == 0;
        });
    }

    List<PropertyModel<?>> createPropertyModels(Class<?> aClass) {
        ClassModelBuilder<?> classModelBuilder = new ClassModelBuilder<>(aClass, true);
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            ConventionAnnotationImpl.processPropertyAnnotations(classModelBuilder, propertyModelBuilder);
        }
        ConventionAnnotationImpl.cleanPropertyBuilders(classModelBuilder);
        List<PropertyModel<?>> propertyModels = new ArrayList<>();
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            boolean isIdProperty = propertyModelBuilder.getName().equals(classModelBuilder.getIdPropertyName());
            if (isIdProperty) {
                propertyModelBuilder.readName(ID_PROPERTY_NAME).writeName(ID_PROPERTY_NAME);
            }
            PropertyModel<?> propertyModel = propertyModelBuilder.build();
            if (propertyModel.getError() != null && !propertyModel.getError().isEmpty()) {
                throw new CodecConfigurationException("property error: " + propertyModel.getError() + ", " + propertyModel);
            }
            propertyModels.add(propertyModel);
        }
        return propertyModels;
    }

    boolean isContainerType(Class<?> aClass) {
        return Collection.class.isAssignableFrom(aClass) || Map.class.isAssignableFrom(aClass);
    }

    /**
     * 如果是CGLib代理类，返回被代理类，否则返回参数本身
     */
     Class<?> getCGLibProxyRawClass(Class<?> aClass) {
        return isProxyClass(aClass) ? aClass.getSuperclass() : aClass;
    }

    /**
     * 是否CGlib代理类
     */
    public boolean isProxyClass(Class<?> aClass) {
        return net.sf.cglib.proxy.Factory.class.isAssignableFrom(aClass);
    }

    public boolean hasClassEnhancer(Class<?> aClass) {
        if (!DocumentNode.class.isAssignableFrom(aClass)) {
            return false;
        }
        if (isContainerType(aClass)) {
            return false;
        }
        if (NotProxy.class.isAssignableFrom(aClass)) {
            return false;
        }
        if (GlobalModels.isImmutableType(aClass)) {
            return false;
        }
        return true;
    }

    <T extends DocumentNode> ProxiedDocumentCreator<T> getProxiedDocumentCreator(Class<T> aClass) {
        if (!hasClassEnhancer(aClass)) {
            throw new DocumentClassDefinitionException(aClass + " not has enhancer");
        }
        return DEFAULT_ENHANCER_PROVIDER.get(aClass);
    }

    private final Map<Class<?>, Boolean> REG_IMMUTABLE_TYPES = new ConcurrentHashMap<>();

    private final Map<Class<?>, ExternalMapStringKeyConverter<?>> STRING_KEY_CONVERTER_MAP = new ConcurrentHashMap<>();

    GlobalModelsImpl() {
        STRING_KEY_CONVERTER_MAP.put(Integer.class, DefaultMapStringKeyConverters.INTEGER_CONVERTER);
        STRING_KEY_CONVERTER_MAP.put(int.class, DefaultMapStringKeyConverters.INTEGER_CONVERTER);
        STRING_KEY_CONVERTER_MAP.put(Long.class, DefaultMapStringKeyConverters.LONG_CONVERTER);
        STRING_KEY_CONVERTER_MAP.put(long.class, DefaultMapStringKeyConverters.LONG_CONVERTER);
        STRING_KEY_CONVERTER_MAP.put(ObjectId.class, DefaultMapStringKeyConverters.OBJECT_ID_CONVERTER);
        STRING_KEY_CONVERTER_MAP.put(String.class, DefaultMapStringKeyConverters.STRING_CONVERTER);
    }

    void regImmutableType(Class<?> aClass, boolean isImmutable) {
        Boolean p = REG_IMMUTABLE_TYPES.putIfAbsent(aClass, isImmutable);
        if (p != null) {
            throw new IllegalStateException("Already register " + aClass + "as immutable:" + p);
        }
    }

    boolean isImmutableType(Class<?> aClass) {
        if (ClassUtils.isPrimitiveOrWrapper(aClass)) {
            return true;
        }
        if (aClass.getAnnotation(ImmutableDocument.class) != null) {
            return true;
        }
        if (KnownTypes.isSimpleAndImmutableType(aClass)) {
            return true;
        }
        if (isContainerType(aClass)) {
            return false;
        }
        if (DtBsonValue.class.isAssignableFrom(aClass)) {
            return true;
        }

        return InternalUtils.mapComputeIfAbsent(REG_IMMUTABLE_TYPES, aClass, aClass1 -> {
            List<PropertyModel<?>> propertyModels = createPropertyModels(aClass1);
            for (PropertyModel<?> propertyModel : propertyModels) {
                if (propertyModel.isWritable() && propertyModel.getPropertyMetadata().isDeserializable()) {
                    return false;
                }
            }
            return true;
        });
    }

    <T> void regMapStringKeyConverter(Class<T> aClass, ExternalMapStringKeyConverter<T> converter) {
        if (!isImmutableType(aClass)) {
            throw new IllegalArgumentException("map key type must be immutable");
        }
        if (aClass.equals(String.class)) {
            throw new IllegalArgumentException();
        }
        STRING_KEY_CONVERTER_MAP.put(aClass, converter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    <T> ExternalMapStringKeyConverter<T> getOrCreateStringKeyConverter(Class<T> k) {
        return (ExternalMapStringKeyConverter<T>) STRING_KEY_CONVERTER_MAP.computeIfAbsent(k, keyType -> {
            boolean isEnumMap = isEnumMap(keyType);
            ExternalMapStringKeyConverter<?> converter = null;
            if (isEnumMap || Enum.class.isAssignableFrom(keyType)) {
                converter = DefaultMapStringKeyConverters.createEnumConverter((Class<Enum>) keyType);
            }
            if (converter == null) {
                if (MapStringKeyConvertable.class.isAssignableFrom(keyType)) {
                    converter = DefaultMapStringKeyConverters.createFromStringConvertable((Class<MapStringKeyConvertable>) keyType);
                } else {
                    throw new CodecConfigurationException(format("Invalid map key type. Map key type must Meet one of the conditions:" +
                            "1. Is String Type, " +
                            "2. Implements MapStringKeyConvertable interface, " +
                            "3. Register ExternalMapStringKeyConverter through GlobalModels.regMapStringKeyConverter. key type : %s", keyType));
                }
            }
            return converter;
        });
    }

    boolean isEnumMap(Class<?> aClass) {
        if (EnumMap.class.isAssignableFrom(aClass) || DocumentEnumMap.class.isAssignableFrom(aClass)) {
            return true;
        } else {
            return false;
        }
    }

}
