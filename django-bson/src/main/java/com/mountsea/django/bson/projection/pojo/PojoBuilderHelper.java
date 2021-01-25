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
package com.mountsea.django.bson.projection.pojo;

import com.mountsea.django.bson.projection.DocumentNode;
import org.bson.codecs.configuration.CodecConfigurationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static com.mountsea.django.bson.projection.pojo.PropertyReflectionUtils.*;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isProtected;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static org.bson.assertions.Assertions.notNull;

final class PojoBuilderHelper {

    @SuppressWarnings("unchecked")
    static <T> void configureClassModelBuilder(final ClassModelBuilder<T> classModelBuilder, final Class<T> clazz,
                                               boolean propertyOnly) {
        classModelBuilder.type(notNull("clazz", clazz));

        ArrayList<Annotation> annotations = new ArrayList<Annotation>();
        Set<String> propertyNames = new TreeSet<String>();
        Map<String, TypeParameterMap> propertyTypeParameterMap = new HashMap<String, TypeParameterMap>();
        Class<? super T> currentClass = clazz;
        String declaringClassName =  clazz.getSimpleName();
        TypeData<?> parentClassTypeData = null;

        Map<String, PropertyMetadata<?>> propertyNameMap = new HashMap<String, PropertyMetadata<?>>();
        while (!currentClass.isEnum() && currentClass.getSuperclass() != null) {
            annotations.addAll(asList(currentClass.getDeclaredAnnotations()));
            List<String> genericTypeNames = new ArrayList<String>();
            for (TypeVariable<? extends Class<? super T>> classTypeVariable : currentClass.getTypeParameters()) {
                genericTypeNames.add(classTypeVariable.getName());
            }

            PropertyReflectionUtils.PropertyMethods propertyMethods = getPropertyMethods(currentClass);

            // Note that we're processing setters before getters. It's typical for setters to have more general types
            // than getters (e.g.: getter returning ImmutableList, but setter accepting Collection), so by evaluating
            // setters first, we'll initialize the PropertyMetadata with the more general type
            for (Method method : propertyMethods.getSetterMethods()) {
                String propertyName = toPropertyName(method);
                propertyNames.add(propertyName);
                PropertyMetadata<?> propertyMetadata = getOrCreateMethodPropertyMetadata(propertyName, declaringClassName, propertyNameMap,
                        TypeData.newInstance(method), propertyTypeParameterMap, parentClassTypeData, genericTypeNames,
                        getGenericType(method));

                if (propertyMetadata.getSetter() == null) {
                    propertyMetadata.setSetter(method);
                    for (Annotation annotation : method.getDeclaredAnnotations()) {
                        propertyMetadata.addWriteAnnotation(annotation);
                    }
                }
            }

            for (Method method : propertyMethods.getGetterMethods()) {
                String propertyName = toPropertyName(method);
                propertyNames.add(propertyName);
                // If the getter is overridden in a subclass, we only want to process that property, and ignore
                // potentially less specific methods from super classes
                PropertyMetadata<?> propertyMetadata = propertyNameMap.get(propertyName);
                if (propertyMetadata != null && propertyMetadata.getGetter() != null) {
                    continue;
                }
                propertyMetadata = getOrCreateMethodPropertyMetadata(propertyName, declaringClassName, propertyNameMap,
                                        TypeData.newInstance(method), propertyTypeParameterMap, parentClassTypeData, genericTypeNames,
                                        getGenericType(method));
                if (propertyMetadata.getGetter() == null) {
                    propertyMetadata.setGetter(method);
                    for (Annotation annotation : method.getDeclaredAnnotations()) {
                        propertyMetadata.addReadAnnotation(annotation);
                    }
                }
            }

            for (Field field : currentClass.getDeclaredFields()) {
                propertyNames.add(field.getName());
                // Note if properties are present and types don't match, the underlying field is treated as an implementation detail.
                PropertyMetadata<?> propertyMetadata = getOrCreateFieldPropertyMetadata(field.getName(), declaringClassName,
                        propertyNameMap, TypeData.newInstance(field), propertyTypeParameterMap, parentClassTypeData, genericTypeNames,
                        field.getGenericType());
                if (propertyMetadata != null && propertyMetadata.getField() == null) {
                    propertyMetadata.field(field);
                    for (Annotation annotation : field.getDeclaredAnnotations()) {
                        propertyMetadata.addReadAnnotation(annotation);
                        propertyMetadata.addWriteAnnotation(annotation);
                    }
                }
            }

            parentClassTypeData = TypeData.newInstance(currentClass.getGenericSuperclass(), currentClass);
            currentClass = currentClass.getSuperclass();
        }

        if (currentClass.isInterface()) {
            annotations.addAll(asList(currentClass.getDeclaredAnnotations()));
        }

        for (String propertyName : propertyNames) {
            PropertyMetadata<?> propertyMetadata = propertyNameMap.get(propertyName);
            if (DocumentNode.class.isAssignableFrom(clazz)) {
                //Only properties with matching fields are supported for DocumentNode class
                if (propertyMetadata.getField() != null &&
                        (propertyMetadata.getGetter() != null || propertyMetadata.getSetter() != null)) {
                    classModelBuilder.addProperty(createPropertyModelBuilder(propertyMetadata));
                }
            } else {
                if (propertyMetadata.isSerializable() || propertyMetadata.isDeserializable()) {
                    classModelBuilder.addProperty(createPropertyModelBuilder(propertyMetadata));
                }
            }
        }

        reverse(annotations);
        classModelBuilder.annotations(annotations);
        classModelBuilder.propertyNameToTypeParameterMap(propertyTypeParameterMap);

        if (propertyOnly) {
            return;
        }

        Constructor<T> noArgsConstructor = null;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length == 0
                    && (isPublic(constructor.getModifiers()) || isProtected(constructor.getModifiers()))) {
                noArgsConstructor = (Constructor<T>) constructor;
                noArgsConstructor.setAccessible(true);
            }
        }

        CreateInstanceInvoker<?> createInstanceInvoker = null;
        if (noArgsConstructor != null) {
            if (GlobalModels.hasClassEnhancer(clazz)) {
                createInstanceInvoker = getCreateInstanceInvokerByEnhancer(clazz);
            } else {
                createInstanceInvoker = getCreateInstanceInvoker(noArgsConstructor);
            }
        }

        classModelBuilder.instanceCreatorFactory(new InstanceCreatorFactoryImpl<T>(
                new CreatorExecutable<T>(clazz, (CreateInstanceInvoker<T>) createInstanceInvoker)));
    }

    private static <T, S> PropertyMetadata<T> getOrCreateMethodPropertyMetadata(final String propertyName,
                                                                                final String declaringClassName,
                                                                                final Map<String, PropertyMetadata<?>> propertyNameMap,
                                                                                final TypeData<T> typeData,
                                                                                final Map<String, TypeParameterMap> propertyTypeParameterMap,
                                                                                final TypeData<S> parentClassTypeData,
                                                                                final List<String> genericTypeNames,
                                                                                final Type genericType) {
        PropertyMetadata<T> propertyMetadata = getOrCreatePropertyMetadata(propertyName, declaringClassName, propertyNameMap, typeData, genericType);
        if (!isAssignableClass(propertyMetadata.getTypeData().getType(), typeData.getType())) {
            propertyMetadata.setError(format("Property '%s' in %s, has differing data types: %s and %s.", propertyName,
                    declaringClassName, propertyMetadata.getTypeData(), typeData));
        }
        cachePropertyTypeData(propertyMetadata, propertyTypeParameterMap, parentClassTypeData, genericTypeNames, genericType);
        return propertyMetadata;
    }

    private static boolean isAssignableClass(final Class<?> propertyTypeClass, final Class<?> typeDataClass) {
        return propertyTypeClass.isAssignableFrom(typeDataClass) || typeDataClass.isAssignableFrom(propertyTypeClass);
    }

    private static <T, S> PropertyMetadata<T> getOrCreateFieldPropertyMetadata(final String propertyName,
                                                                               final String declaringClassName,
                                                                               final Map<String, PropertyMetadata<?>> propertyNameMap,
                                                                               final TypeData<T> typeData,
                                                                               final Map<String, TypeParameterMap> propertyTypeParameterMap,
                                                                               final TypeData<S> parentClassTypeData,
                                                                               final List<String> genericTypeNames,
                                                                               final Type genericType) {
        PropertyMetadata<T> propertyMetadata = getOrCreatePropertyMetadata(propertyName, declaringClassName, propertyNameMap, typeData, genericType);
        if (!propertyMetadata.getTypeData().getType().isAssignableFrom(typeData.getType())) {
            return null;
        }
        cachePropertyTypeData(propertyMetadata, propertyTypeParameterMap, parentClassTypeData, genericTypeNames, genericType);
        return propertyMetadata;
    }

    @SuppressWarnings("unchecked")
    private static <T> PropertyMetadata<T> getOrCreatePropertyMetadata(final String propertyName,
                                                                       final String declaringClassName,
                                                                       final Map<String, PropertyMetadata<?>> propertyNameMap,
                                                                       final TypeData<T> typeData,
                                                                       final Type genericType) {
        PropertyMetadata<T> propertyMetadata = (PropertyMetadata<T>) propertyNameMap.get(propertyName);
        if (propertyMetadata == null) {
            propertyMetadata = new PropertyMetadata<T>(propertyName, declaringClassName, typeData, genericType);
            propertyNameMap.put(propertyName, propertyMetadata);
        }
        return propertyMetadata;
    }

    private static <T, S> void cachePropertyTypeData(final PropertyMetadata<T> propertyMetadata,
                                                     final Map<String, TypeParameterMap> propertyTypeParameterMap,
                                                     final TypeData<S> parentClassTypeData,
                                                     final List<String> genericTypeNames,
                                                     final Type genericType) {
        TypeParameterMap typeParameterMap = getTypeParameterMap(genericTypeNames, genericType);
        propertyTypeParameterMap.put(propertyMetadata.getName(), typeParameterMap);
        propertyMetadata.typeParameterInfo(typeParameterMap, parentClassTypeData);
    }

    private static Type getGenericType(final Method method) {
        return isGetter(method) ? method.getGenericReturnType() : method.getGenericParameterTypes()[0];
    }

    @SuppressWarnings("unchecked")
    static <T> PropertyModelBuilder<T> createPropertyModelBuilder(final PropertyMetadata<T> propertyMetadata) {
        PropertyModelBuilder<T> propertyModelBuilder = PropertyModel.<T>builder()
                .propertyName(propertyMetadata.getName())
                .readName(propertyMetadata.getName())
                .writeName(propertyMetadata.getName())
                .typeData(propertyMetadata.getTypeData())
                .readAnnotations(propertyMetadata.getReadAnnotations())
                .writeAnnotations(propertyMetadata.getWriteAnnotations())
                .propertySerialization(new PropertyModelSerializationImpl<T>())
                .propertyAccessor(new PropertyAccessorImpl<T>(propertyMetadata))
                .propertyMetadata(propertyMetadata)
                .setError(propertyMetadata.getError());

        if (propertyMetadata.getTypeParameters() != null) {
            specializePropertyModelBuilder(propertyModelBuilder, propertyMetadata);
        }

        return propertyModelBuilder;
    }

    private static TypeParameterMap getTypeParameterMap(final List<String> genericTypeNames, final Type propertyType) {
        int classParamIndex = genericTypeNames.indexOf(propertyType.toString());
        TypeParameterMap.Builder builder = TypeParameterMap.builder();
        if (classParamIndex != -1) {
            builder.addIndex(classParamIndex);
        } else {
            if (propertyType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) propertyType;
                for (int i = 0; i < pt.getActualTypeArguments().length; i++) {
                    classParamIndex = genericTypeNames.indexOf(pt.getActualTypeArguments()[i].toString());
                    if (classParamIndex != -1) {
                        builder.addIndex(i, classParamIndex);
                    }
                }
            }
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <V> void specializePropertyModelBuilder(final PropertyModelBuilder<V> propertyModelBuilder,
                                                           final PropertyMetadata<V> propertyMetadata) {
        if (propertyMetadata.getTypeParameterMap().hasTypeParameters() && !propertyMetadata.getTypeParameters().isEmpty()) {
            TypeData<V> specializedFieldType;
            Map<Integer, Integer> fieldToClassParamIndexMap = propertyMetadata.getTypeParameterMap().getPropertyToClassParamIndexMap();
            Integer classTypeParamRepresentsWholeField = fieldToClassParamIndexMap.get(-1);
            if (classTypeParamRepresentsWholeField != null) {
                specializedFieldType = (TypeData<V>) propertyMetadata.getTypeParameters().get(classTypeParamRepresentsWholeField);
            } else {
                TypeData.Builder<V> builder = TypeData.builder(propertyModelBuilder.getTypeData().getType());
                List<TypeData<?>> typeParameters = new ArrayList<TypeData<?>>(propertyModelBuilder.getTypeData().getTypeParameters());
                for (int i = 0; i < typeParameters.size(); i++) {
                    for (Map.Entry<Integer, Integer> mapping : fieldToClassParamIndexMap.entrySet()) {
                        if (mapping.getKey().equals(i)) {
                            typeParameters.set(i, propertyMetadata.getTypeParameters().get(mapping.getValue()));
                        }
                    }
                }
                builder.addTypeParameters(typeParameters);
                specializedFieldType = builder.build();
            }
            propertyModelBuilder.typeData(specializedFieldType);
        }
    }

    static <V> V stateNotNull(final String property, final V value) {
        if (value == null) {
            throw new IllegalStateException(format("%s cannot be null", property));
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> CreateInstanceInvoker<T> getCreateInstanceInvokerByEnhancer(Class aClass) {
        return new CreateInstanceInvoker<T>() {
            @Override
            public T create() {
                return (T) DocumentNode.create(aClass);
            }

            @Override
            public T create(Class<?>[] paramTypes, Object[] params) {
                return (T) DocumentNode.create(aClass, paramTypes, params);
            }
        };
    }

    static <T> CreateInstanceInvoker<T> getCreateInstanceInvoker(Constructor<T> constructor) {
        return new CreateInstanceInvoker<T>() {
            @Override
            public T create() {
                try {
                    return constructor.newInstance();
                } catch (Exception e) {
                    throw new CodecConfigurationException("create instance", e);
                }
            }

            @Override
            public T create(Class<?>[] paramTypes, Object[] params) {
                try {
                    return constructor.newInstance(params);
                } catch (Exception e) {
                    throw new CodecConfigurationException("create instance", e);
                }
            }
        };
    }

    static CreateInstanceInvoker<?> getCreateInstanceInvoker(Method method) {
        return new CreateInstanceInvoker<Object>() {
            @Override
            public Object create() {
                try {
                    return method.invoke(null);
                } catch (Exception e) {
                    throw new CodecConfigurationException("create instance", e);
                }
            }

            @Override
            public Object create(Class<?>[] paramTypes, Object[] params) {
                try {
                    return method.invoke(params);
                } catch (Exception e) {
                    throw new CodecConfigurationException("create instance", e);
                }
            }
        };
    }

    private PojoBuilderHelper() {
    }

}
