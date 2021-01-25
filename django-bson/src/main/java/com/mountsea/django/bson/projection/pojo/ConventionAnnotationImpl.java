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

import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.pojo.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.mountsea.django.bson.projection.pojo.PojoBuilderHelper.createPropertyModelBuilder;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

final class ConventionAnnotationImpl implements Convention {

    @Override
    public void apply(final ClassModelBuilder<?> classModelBuilder) {
        for (final Annotation annotation : classModelBuilder.getAnnotations()) {
            processClassAnnotation(classModelBuilder, annotation);
        }

        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            processPropertyAnnotations(classModelBuilder, propertyModelBuilder);
        }

        processCreatorAnnotation(classModelBuilder);

        cleanPropertyBuilders(classModelBuilder);
    }

    static void processClassAnnotation(final ClassModelBuilder<?> classModelBuilder, final Annotation annotation) {
        if (annotation instanceof BsonDiscriminator) {
            BsonDiscriminator discriminator = (BsonDiscriminator) annotation;
            String key = discriminator.key();
            if (!key.equals("")) {
                classModelBuilder.discriminatorKey(key);
            }

            String name = discriminator.value();
            if (!name.equals("")) {
                classModelBuilder.discriminator(name);
            }
            classModelBuilder.enableDiscriminator(true);
        }
    }

    static void processPropertyAnnotations(final ClassModelBuilder<?> classModelBuilder,
                                            final PropertyModelBuilder<?> propertyModelBuilder) {
        for (Annotation annotation : propertyModelBuilder.getReadAnnotations()) {
            if (annotation instanceof BsonProperty) {
                BsonProperty bsonProperty = (BsonProperty) annotation;
                if (!"".equals(bsonProperty.value())) {
                    propertyModelBuilder.readName(bsonProperty.value());
                }
                propertyModelBuilder.discriminatorEnabled(bsonProperty.useDiscriminator());
                if (propertyModelBuilder.getName().equals(classModelBuilder.getIdPropertyName())) {
                    classModelBuilder.idPropertyName(null);
                }
            } else if (annotation instanceof BsonId) {
                classModelBuilder.idPropertyName(propertyModelBuilder.getName());
            } else if (annotation instanceof BsonIgnore) {
                propertyModelBuilder.readName(null);
            }
        }

        for (Annotation annotation : propertyModelBuilder.getWriteAnnotations()) {
            if (annotation instanceof BsonProperty) {
                BsonProperty bsonProperty = (BsonProperty) annotation;
                if (!"".equals(bsonProperty.value())) {
                    propertyModelBuilder.writeName(bsonProperty.value());
                }
            } else if (annotation instanceof BsonIgnore) {
                propertyModelBuilder.writeName(null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    static  <T> void processCreatorAnnotation(final ClassModelBuilder<T> classModelBuilder) {
        Class<T> clazz = classModelBuilder.getType();

        CreateInstanceInvoker<T> enhancerCreateInstanceInvoker = null;
        if (GlobalModels.hasClassEnhancer(clazz)) {
            enhancerCreateInstanceInvoker = PojoBuilderHelper.getCreateInstanceInvokerByEnhancer(clazz);
        }

        CreatorExecutable<T> creatorExecutable = null;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (isPublic(constructor.getModifiers()) && !constructor.isSynthetic()) {
                for (Annotation annotation : constructor.getDeclaredAnnotations()) {
                    if (annotation.annotationType().equals(BsonCreator.class)) {
                        if (creatorExecutable != null) {
                            throw new CodecConfigurationException("Found multiple constructors annotated with @BsonCreator");
                        }
                        if (enhancerCreateInstanceInvoker == null) {
                            enhancerCreateInstanceInvoker = (CreateInstanceInvoker<T>) PojoBuilderHelper.getCreateInstanceInvoker(constructor);
                        }
                        creatorExecutable = new CreatorExecutable<T>(clazz, enhancerCreateInstanceInvoker,
                                constructor.getParameterTypes(), constructor.getGenericParameterTypes(),
                                constructor.getParameterAnnotations());
                    }
                }
            }
        }

        Class<?> bsonCreatorClass = clazz;
        boolean foundStaticBsonCreatorMethod = false;
        while (bsonCreatorClass != null && !foundStaticBsonCreatorMethod) {
            for (Method method : bsonCreatorClass.getDeclaredMethods()) {
                if (isStatic(method.getModifiers()) && !method.isSynthetic() && !method.isBridge()) {
                    for (Annotation annotation : method.getDeclaredAnnotations()) {
                        if (annotation.annotationType().equals(BsonCreator.class)) {
                            if (creatorExecutable != null) {
                                throw new CodecConfigurationException("Found multiple constructors / methods annotated with @BsonCreator");
                            } else if (!bsonCreatorClass.isAssignableFrom(method.getReturnType())) {
                                throw new CodecConfigurationException(
                                        format("Invalid method annotated with @BsonCreator. Returns '%s', expected %s",
                                                method.getReturnType(), bsonCreatorClass));
                            }
                            if (enhancerCreateInstanceInvoker != null) {
                                throw new CodecConfigurationException(
                                        "Not support using @BsonCreator on DocumentNode class static method." + bsonCreatorClass);
                            }
                            creatorExecutable = new CreatorExecutable<T>(clazz,
                                    (CreateInstanceInvoker<T>) PojoBuilderHelper.getCreateInstanceInvoker(method));
                            foundStaticBsonCreatorMethod = true;
                        }
                    }
                }
            }

            bsonCreatorClass = bsonCreatorClass.getSuperclass();
        }

        if (creatorExecutable != null) {
            List<BsonProperty> properties = creatorExecutable.getProperties();
            List<Class<?>> parameterTypes = creatorExecutable.getParameterTypes();
            List<Type> parameterGenericTypes = creatorExecutable.getParameterGenericTypes();

            if (properties.size() != parameterTypes.size()) {
                throw creatorExecutable.getError(clazz, "All parameters in the @BsonCreator method / constructor must be annotated "
                                + "with a @BsonProperty.");
            }
            for (int i = 0; i < properties.size(); i++) {
                boolean isIdProperty = creatorExecutable.getIdPropertyIndex() != null && creatorExecutable.getIdPropertyIndex().equals(i);
                Class<?> parameterType = parameterTypes.get(i);
                Type genericType = parameterGenericTypes.get(i);
                PropertyModelBuilder<?> propertyModelBuilder = null;

                if (isIdProperty) {
                    propertyModelBuilder = classModelBuilder.getProperty(classModelBuilder.getIdPropertyName());
                } else {
                    BsonProperty bsonProperty = properties.get(i);

                    // Find the property using write name and falls back to read name
                    for (PropertyModelBuilder<?> builder : classModelBuilder.getPropertyModelBuilders()) {
                        if (bsonProperty.value().equals(builder.getWriteName())) {
                            propertyModelBuilder = builder;
                            break;
                        } else if (bsonProperty.value().equals(builder.getReadName())) {
                            // When there is a property that matches the read name of the parameter, save it but continue to look
                            // This is just in case there is another property that matches the write name.
                            propertyModelBuilder = builder;
                        }
                    }

                    // Support legacy options, when BsonProperty matches the actual POJO property name (e.g. method name or field name).
                    if (propertyModelBuilder == null) {
                        propertyModelBuilder = classModelBuilder.getProperty(bsonProperty.value());
                    }

                    if (propertyModelBuilder == null) {
                        propertyModelBuilder = addCreatorPropertyToClassModelBuilder(classModelBuilder, bsonProperty.value(),
                            parameterType);
                    } else {
                        // If not using a legacy BsonProperty reference to the property set the write name to be the annotated name.
                        if (!bsonProperty.value().equals(propertyModelBuilder.getName())) {
                            propertyModelBuilder.writeName(bsonProperty.value());
                        }
                        tryToExpandToGenericType(parameterType, propertyModelBuilder, genericType);
                    }
                }

                if (!propertyModelBuilder.getTypeData().isAssignableFrom(parameterType)) {
                    throw creatorExecutable.getError(clazz, format("Invalid Property type for '%s'. Expected %s, found %s.",
                        propertyModelBuilder.getWriteName(), propertyModelBuilder.getTypeData().getType(), parameterType));
                }
            }
            classModelBuilder.instanceCreatorFactory(new InstanceCreatorFactoryImpl<T>(creatorExecutable));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void tryToExpandToGenericType(final Class<?> parameterType, final PropertyModelBuilder<T> propertyModelBuilder,
                                                     final Type genericType) {
        if (parameterType.isAssignableFrom(propertyModelBuilder.getTypeData().getType())) {
            // The existing getter for this field returns a more specific type than what the constructor accepts
            // This is typical when the getter returns a specific subtype, but the constructor accepts a more
            // general one (e.g.: getter returns ImmutableList<T>, while constructor just accepts List<T>)
            propertyModelBuilder.typeData(TypeData.newInstance(genericType, (Class<T>) parameterType));
        }
    }

    static <T, S> PropertyModelBuilder<S> addCreatorPropertyToClassModelBuilder(final ClassModelBuilder<T> classModelBuilder,
                                                                                final String name,
                                                                                final Class<S> clazz) {
        PropertyModelBuilder<S> propertyModelBuilder = createPropertyModelBuilder(new PropertyMetadata<S>(name,
            classModelBuilder.getType().getSimpleName(), TypeData.builder(clazz).build(), clazz)).readName(null).writeName(name);
        classModelBuilder.addProperty(propertyModelBuilder);
        return propertyModelBuilder;
    }

    static void cleanPropertyBuilders(final ClassModelBuilder<?> classModelBuilder) {
        List<String> propertiesToRemove = new ArrayList<String>();
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            if (!propertyModelBuilder.isReadable() && !propertyModelBuilder.isWritable()) {
                propertiesToRemove.add(propertyModelBuilder.getName());
            }
        }
        for (String propertyName : propertiesToRemove) {
            classModelBuilder.removeProperty(propertyName);
        }
    }
}
