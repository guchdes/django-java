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

import com.mongodb.client.model.Filters;
import io.github.guchdes.django.bson.BsonDocumentBuilder;
import io.github.guchdes.django.bson.BsonUtils;
import io.github.guchdes.django.bson.projection.NotAsRecordRoot;
import io.github.guchdes.django.bson.projection.pojo.GlobalModels;
import io.github.guchdes.django.bson.annotation.FillNullByEmpty;
import io.github.guchdes.django.bson.projection.DocumentClassDefinitionException;
import io.github.guchdes.django.bson.projection.pojo.*;
import io.github.guchdes.django.bson.util.InternalUtils;
import io.github.guchdes.django.core.CollectibleDocumentDefinition.Property;
import io.github.guchdes.django.core.annotation.*;
import io.github.guchdes.django.core.exception.IllegalKeyDjangoException;
import io.github.guchdes.django.core.util.ArrayAttributeMap;
import io.github.guchdes.django.core.util.AttributeMap;
import io.github.guchdes.django.core.util.DefaultAttributeMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author guch
 * @Since 3.0.0
 */
@Slf4j
public class CollectibleDocumentDefinitions {

    private static final String ID = "id";
    private static final String _ID = "_id";

    private static final Map<Class<?>, CollectibleDocumentDefinition> documentDefinitionMap = new ConcurrentHashMap<>();

    public static CollectibleDocumentDefinition getDocumentDefinition(Class<?> documentClass) {
        documentClass = getRealDocumentClass(documentClass);
        return documentDefinitionMap.computeIfAbsent(documentClass,
                CollectibleDocumentDefinitions::generateDocumentDefinition);
    }

    public static Class<?> getRealDocumentClass(Class<?> documentClass) {
        while (isCGLIBClass(documentClass)) {
            documentClass = documentClass.getSuperclass();
        }
        return documentClass;
    }

    private static boolean isCGLIBClass(Class<?> documentClass) {
        return documentClass.getName() != null && documentClass.getName().contains("CGLIB$$");
    }

    @SuppressWarnings("unchecked")
    private static Property<Object> getIdProperty(Map<String, Property<?>> propertyMap, Class<?> documentClass) {
        Property<?> idProperty = propertyMap.get(ID);
        if (idProperty == null) {
            idProperty = propertyMap.get(_ID);
        }
        if (idProperty == null) {
            throw new DocumentClassDefinitionException("No id property found:" + documentClass);
        }
        return (Property<Object>) idProperty;
    }

    @SuppressWarnings("unchecked")
    private static CollectibleDocumentDefinition generateDocumentDefinition(Class<?> documentClass) {
        if (NotAsRecordRoot.class.isAssignableFrom(documentClass)) {
            throw new DocumentClassDefinitionException(documentClass + " is NotAsRecordRoot");
        }
        LinkedHashMap<String, Property<?>> propertyMap = getPropertyMap(documentClass);
        Property<Object> idProperty = getIdProperty(propertyMap, documentClass);
        return new CollectibleDocumentDefinitionImpl((Class<? extends CollectibleDocument>) documentClass,
                getCollectionName(documentClass), idProperty, propertyMap,
                getKeyDefinition(documentClass, propertyMap), getVersionFieldName(documentClass),
                isFillNullToEmpty(documentClass),
                isAllowNullKeyField(documentClass));
    }

    private static boolean isFillNullToEmpty(Class<?> aClass) {
        return aClass.getAnnotation(FillNullByEmpty.class) != null;
    }

    private static boolean isAllowNullKeyField(Class<?> aClass) {
        return aClass.getAnnotation(AllowNullKeyField.class) != null;
    }

    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Property<?>> getPropertyMap(Class<?> aClass) {
        ClassModel<?> classModelInfo = GlobalModels.getClassModel(aClass);
        List<PropertyModel<?>> propertyModels = classModelInfo.getPropertyModels();
        LinkedHashMap<String, Property<?>> propertyMap = new LinkedHashMap<>();
        for (PropertyModel<?> propertyModel : propertyModels) {
            String name = propertyModel.getName();
            if (propertyModel.isReadable() && propertyModel.isWritable()) {
                PropertyAccessor<Object> propertyAccessor = (PropertyAccessor<Object>) propertyModel.getPropertyAccessor();
                propertyMap.put(name, new AbstractProperty<Object>(name, propertyModel.getGenericType()) {
                    @Override
                    public void set(Object document, Object fieldValue) {
                        propertyAccessor.set(document, fieldValue);
                    }

                    @Override
                    public Object get(Object document) {
                        return propertyAccessor.get(document);
                    }
                });
            } else {
                log.warn("property not readable or not writeable:" + propertyModel);
            }
        }
        return propertyMap;
    }

    @Nullable
    private static String getVersionFieldName(Class<?> aClass) {
        List<Field> fields = InternalUtils.findFieldsUpward(aClass, field -> field.getAnnotation(VersionField.class) != null);
        if (fields.size() > 1) {
            throw new DocumentClassDefinitionException("multi @VersionField annotated find:" + fields);
        } else if (fields.isEmpty()) {
            return null;
        } else {
            return fields.get(0).getName();
        }
    }

    private static String getCollectionName(Class<?> documentClass) {
        CollectionName collectionName = documentClass.getAnnotation(CollectionName.class);
        if (collectionName != null) {
            if (StringUtils.isBlank(collectionName.value())) {
                throw new DocumentClassDefinitionException("CollectionName is blank:" + documentClass);
            } else {
                return collectionName.value();
            }
        } else {
            return documentClass.getSimpleName();
        }
    }

    private static class AnnotatedKey {
        final Class<?> keyClass;
        final String singleKeyFieldName;
        final Type singleKeyFieldType;

        public AnnotatedKey(Class<?> keyClass) {
            this.keyClass = keyClass;
            singleKeyFieldName = null;
            singleKeyFieldType = null;
        }

        public AnnotatedKey(String singleKeyFieldName, Type singleKeyFieldType) {
            this.singleKeyFieldName = singleKeyFieldName;
            Class<?> rawType = InternalUtils.getRawType(singleKeyFieldType);
            if (rawType.isPrimitive()) {
                singleKeyFieldType = ClassUtils.primitiveToWrapper(rawType);
            }
            this.singleKeyFieldType = singleKeyFieldType;
            keyClass = null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, PropertyModel<Object>> getPropertyModelMap(Class<?> documentClass) {
        ClassModel<?> classModelInfo = GlobalModels.getClassModel(documentClass);
        Map<String, PropertyModel<?>> propMap = classModelInfo.getPropertyModels().stream().collect(
                Collectors.toMap(PropertyModel::getName, x -> x));
        return (Map<String, PropertyModel<Object>>) ((Object) propMap);
    }

    /**
     * 从文档类和其父类中查找标注的key
     */
    @Nullable
    private static AnnotatedKey getAnnotatedKey(Class<?> documentClass) {
        if (documentClass.isAssignableFrom(CollectibleDocument.class)) {
            return null;
        }
        KeyClass annotation = documentClass.getAnnotation(KeyClass.class);
        if (annotation != null) {
            return new AnnotatedKey(annotation.value());
        }
        KeyFieldName keyFieldNameAnnotation = documentClass.getAnnotation(KeyFieldName.class);
        if (keyFieldNameAnnotation != null) {
            String name = keyFieldNameAnnotation.value();
            Map<String, PropertyModel<Object>> map = getPropertyModelMap(documentClass);
            PropertyModel<Object> propertyModelInfo = map.get(name);
            if (propertyModelInfo == null) {
                throw new DocumentClassDefinitionException("Not find property " + name + " in class "
                        + documentClass + " and its parent classes");
            }
            return new AnnotatedKey(name, propertyModelInfo.getTypeData().getType());
        }
        Field keyField = null;
        Field[] fields = documentClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(KeyField.class) != null) {
                if (keyField == null) {
                    keyField = field;
                } else {
                    throw new DocumentClassDefinitionException("@KeyField cannot use on multi fields:" + documentClass);
                }
            }
        }
        if (keyField != null) {
            return new AnnotatedKey(keyField.getName(), keyField.getType());
        } else {
            if (documentClass.getSuperclass().equals(Object.class)) {
                return null;
            } else {
                return getAnnotatedKey(documentClass.getSuperclass());
            }
        }
    }

    private static void checkKeyProperty(String name, Type type, Map<String, Property<?>> allPropertyMap, Class<?> documentClass) {
        Property<?> property = allPropertyMap.get(name);
        if (property == null) {
            throw new DocumentClassDefinitionException("Document no key property:" + name + ", " + documentClass);
        }
        if (!property.getType().equals(type)) {
            throw new DocumentClassDefinitionException("key property type not matches:" + name + ", " + documentClass);
        }
    }

    private static CollectibleDocumentDefinition.KeyDefinition getKeyDefinition(Class<?> documentClass, Map<String, Property<?>> propertyMap) {
        AnnotatedKey annotatedKey = getAnnotatedKey(documentClass);
        Property<Object> idProperty = getIdProperty(propertyMap, documentClass);
        boolean isId = false;
        LinkedHashMap<String, Property<?>> keyProps = new LinkedHashMap<>();
        if (annotatedKey == null) {
            keyProps.put(ID, idProperty);
            isId = true;
        } else {
            if (annotatedKey.keyClass != null) {
                Map<String, Property<?>> keyClassPropertyMap = getPropertyMap(annotatedKey.keyClass);
                if (keyClassPropertyMap.size() == 0) {
                    throw new DocumentClassDefinitionException("Not found property in KeyClass:" + annotatedKey.keyClass);
                }
                if (keyClassPropertyMap.containsKey(ID) || keyClassPropertyMap.containsKey(_ID)) {
                    throw new DocumentClassDefinitionException("Not allow KeyClass contains id. " + documentClass);
                }
                for (Map.Entry<String, Property<?>> entry : keyClassPropertyMap.entrySet()) {
                    checkKeyProperty(entry.getKey(), entry.getValue().getType(), propertyMap, documentClass);
                }
                keyProps.putAll(keyClassPropertyMap);
            } else {
                if (!propertyMap.containsKey(annotatedKey.singleKeyFieldName)) {
                    throw new DocumentClassDefinitionException("Key field name " + annotatedKey.singleKeyFieldName +
                            " is not class property. " + documentClass);
                }
                String name = annotatedKey.singleKeyFieldName;
                Type type = annotatedKey.singleKeyFieldType;
                if (name.equals(ID) || name.equals(_ID)) {
                    isId = true;
                }
                keyProps.put(annotatedKey.singleKeyFieldName, new AbstractProperty<Object>(name, type) {
                    @Override
                    public void set(Object document, Object fieldValue) {
                        throw new UnsupportedOperationException("not class type key");
                    }

                    @Override
                    public Object get(Object document) {
                        throw new UnsupportedOperationException("not class type key");
                    }
                });
            }
        }
        log.info("key Fields of class " + documentClass + " is :" + keyProps.keySet());
        Class<?> keyClass = annotatedKey == null ? null : annotatedKey.keyClass;
        boolean isSingleField = keyProps.size() == 1;
        boolean isSingleFieldAndMultiProp = false;
        LinkedHashMap<String, Property<?>> singleFieldMultiPropMap = new LinkedHashMap<>(0);
        if (isSingleField) {
            Property<?> keySingleProp = keyProps.values().iterator().next();
            isSingleFieldAndMultiProp = !GlobalModels.isSimpleType(keySingleProp.getRawClass());
            if (isSingleFieldAndMultiProp) {
                singleFieldMultiPropMap = getPropertyMap(keySingleProp.getRawClass());
            }
        }
        return new KeyDefinitionImpl(isId, keyProps, keyClass,
                getKeyExtractor(documentClass, keyClass, keyProps.keySet(), isId),
                getBsonKeyConverter(documentClass, keyClass, keyProps, isId),
                isSingleFieldAndMultiProp, singleFieldMultiPropMap);
    }

    private static CollectibleDocumentDefinition.KeyExtractor getKeyExtractor(Class<?> documentClass, @Nullable Class<?> keyClass,
                                                                              Set<String> keyPropertyNames, boolean isId) {
        ClassModel<?> documentClassModel = GlobalModels.getClassModel(documentClass);
        Map<String, PropertyModel<?>> propMap = documentClassModel.getPropertyModels().stream().collect(
                Collectors.toMap(PropertyModel::getName, x -> x));
        if (keyClass == null) {
            String fieldName = keyPropertyNames.iterator().next();
            PropertyModel<?> propertyModelInfo = propMap.get(fieldName);
            return new CollectibleDocumentDefinition.KeyExtractor() {
                @Override
                public Object extractKey(CollectibleDocument document, boolean allowNullField) throws IllegalKeyDjangoException {
                    Object o = propertyModelInfo.getPropertyAccessor().get(document);
                    if (o == null && !allowNullField) throwsNullKeyField(fieldName, documentClass);
                    return o;
                }

                @Override
                public BsonDocument extractBsonKey(CollectibleDocument document, boolean allowNullField) throws IllegalKeyDjangoException {
                    Object o = propertyModelInfo.getPropertyAccessor().get(document);
                    String name = isId ? _ID : fieldName;
                    if (o == null && !allowNullField) throwsNullKeyField(name, documentClass);
                    return BsonDocumentBuilder.newBuilder(name, o).build();
                }
            };
        } else {
            propMap.keySet().retainAll(keyPropertyNames);
            ClassModel<?> keyClassModel = GlobalModels.getClassModel(keyClass);
            Map<String, PropertyModel<?>> keyClassPropMap = keyClassModel.getPropertyModels().stream().collect(
                    Collectors.toMap(PropertyModel::getName, x -> x));
            return new CollectibleDocumentDefinition.KeyExtractor() {
                @Override
                @SuppressWarnings("unchecked")
                public Object extractKey(CollectibleDocument document, boolean allowNullField) throws IllegalKeyDjangoException {
                    InstanceCreator<?> instanceCreator = keyClassModel.getInstanceCreatorFactory().create();
                    for (Map.Entry<String, PropertyModel<?>> entry : propMap.entrySet()) {
                        PropertyModel<Object> p = (PropertyModel<Object>) keyClassPropMap.get(entry.getKey());
                        Object propValue = entry.getValue().getPropertyAccessor().get(document);
                        if (propValue == null && !allowNullField) throwsNullKeyField(entry.getKey(), documentClass);
                        instanceCreator.set(propValue, p);
                    }
                    return instanceCreator.getInstance();
                }

                @Override
                public BsonDocument extractBsonKey(CollectibleDocument document, boolean allowNullField) throws IllegalKeyDjangoException {
                    //KeyClass中不能包含id属性
                    BsonDocumentBuilder builder = BsonDocumentBuilder.newBuilder();
                    for (Map.Entry<String, PropertyModel<?>> entry : propMap.entrySet()) {
                        Object propValue = entry.getValue().getPropertyAccessor().get(document);
                        if (propValue == null && !allowNullField) throwsNullKeyField(entry.getKey(), documentClass);
                        builder.put(entry.getKey(), propValue);
                    }
                    return builder.build();
                }
            };
        }
    }

    private static CollectibleDocumentDefinition.BsonKeyConverter getBsonKeyConverter(Class<?> documentClass, @Nullable Class<?> keyClass,
                                                                                      Map<String, Property<?>> propertyMap, boolean isId) {
        Property<?> singleProp = keyClass == null ? propertyMap.values().iterator().next() : null;
        String singlePropName = isId ? _ID : (singleProp == null ? "" : singleProp.getPropertyName());
        return new CollectibleDocumentDefinition.BsonKeyConverter() {
            @Override
            public Bson keyToBsonFilter(Object key, CodecRegistry codecRegistry, boolean allowNullField) {
                checkKey(key, allowNullField);
                if (keyClass == null) {
                    return Filters.eq(singlePropName, key);
                } else {
                    return fillNullKeyProperty(documentClass, BsonUtils.toBsonDocument(key, codecRegistry),
                            propertyMap, allowNullField);
                }
            }

            @Override
            public BsonDocument keyToBsonDocument(Object key, CodecRegistry codecRegistry, boolean allowNullField) {
                checkKey(key, allowNullField);
                if (keyClass == null) {
                    return fillNullKeyProperty(documentClass, BsonUtils.toBsonDocument(new Document(singlePropName, key), codecRegistry),
                            propertyMap, allowNullField);
                } else {
                    return fillNullKeyProperty(documentClass, BsonUtils.toBsonDocument(key, codecRegistry),
                            propertyMap, allowNullField);
                }
            }

            private void checkKey(Object key, boolean allowNullField) {
                if (keyClass == null && !allowNullField && key == null) {
                    throwsNullKeyField(singlePropName, documentClass);
                }
                if (key != null) {
                    checkKeyObjectType(documentClass, keyClass, singleProp, key);
                }
            }

            private void checkKeyObjectType(Class<?> documentClass, Class<?> keyClass, Property<?> singleProp, Object key) {
                Objects.requireNonNull(key, "key");
                Class<?> expectClass = keyClass == null ? singleProp.getRawClass() : keyClass;
                if (!ClassUtils.isAssignable(key.getClass(), expectClass)) {
                    throw new IllegalKeyDjangoException(String.format("Key parameter of class %s should be %s, but found %s",
                            documentClass, expectClass, key));
                }
            }

            private BsonDocument fillNullKeyProperty(Class<?> documentClass, BsonDocument bsonDocument,
                                                     Map<String, Property<?>> propertyMap, boolean allowNullField) {
                for (String prop : propertyMap.keySet()) {
                    if (prop.equals(ID)) {
                        //bsonDocument中只使用_id
                        prop = _ID;
                    }
                    if (!bsonDocument.containsKey(prop)) {
                        if (allowNullField) {
                            bsonDocument.put(prop, BsonNull.VALUE);
                        } else {
                            throwsNullKeyField(prop, documentClass);
                        }
                    }
                }
                return bsonDocument;
            }
        };
    }

    private static void throwsNullKeyField(String field, Class<?> aClass) {
        throw new IllegalKeyDjangoException(String.format("key field %s.%s is null", aClass, field));
    }

    private static <T, Y> Map.Entry<T, Y> getMapSingleEntry(Map<T, Y> map) {
        return map.entrySet().iterator().next();
    }

    @Getter
    @AllArgsConstructor
    private static class CollectibleDocumentDefinitionImpl implements CollectibleDocumentDefinition {
        final Class<? extends CollectibleDocument> documentClass;

        final String collectionName;

        final Property<Object> idProperty;

        final LinkedHashMap<String, Property<?>> propertyMap;

        final KeyDefinition keyDefinition;

        final String versionFieldName;

        final boolean fillNullByEmpty;

        final boolean allowNullKeyField;

        final AttributeMap attributeMap = new ArrayAttributeMap();
    }

    @Getter
    @AllArgsConstructor
    private static class KeyDefinitionImpl implements CollectibleDocumentDefinition.KeyDefinition {
        final boolean isId;

        final LinkedHashMap<String, Property<?>> propertyMap;

        final Class<?> annotationKeyClass;

        final CollectibleDocumentDefinition.KeyExtractor keyExtractor;

        final CollectibleDocumentDefinition.BsonKeyConverter bsonKeyConverter;

        final boolean singleFiledAndMultiPropKey;

        final LinkedHashMap<String, Property<?>> singleFieldMultiPropMap;

    }

    private static abstract class AbstractProperty<T> implements Property<T> {
        private final String name;
        private final Type type;

        public AbstractProperty(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String getPropertyName() {
            return name;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public Class<?> getRawClass() {
            return InternalUtils.getRawType(type);
        }
    }
}
