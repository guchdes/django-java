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
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import static java.lang.String.format;
import static java.util.Arrays.asList;

final class CreatorExecutable<T> {
    private final Class<T> clazz;
    private final CreateInstanceInvoker<T> invoker;
    private final List<BsonProperty> properties = new ArrayList<BsonProperty>();
    private final Integer idPropertyIndex;
    private final List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
    private final List<Type> parameterGenericTypes = new ArrayList<Type>();

    CreatorExecutable(final Class<T> clazz, final CreateInstanceInvoker<T> invoker) {
        this(clazz, invoker, new Class<?>[0], new Type[0], new Annotation[0][]);
    }

    CreatorExecutable(final Class<T> clazz, final CreateInstanceInvoker<T> invoker,
                      final Class<?>[] paramTypes, final Type[] genericParamTypes, Annotation[][] parameterAnnotations) {
        this.clazz = clazz;
        this.invoker = invoker;
        Integer idPropertyIndex = null;

        parameterTypes.addAll(asList(paramTypes));
        parameterGenericTypes.addAll(asList(genericParamTypes));

        for (int i = 0; i < parameterAnnotations.length; ++i) {
            Annotation[] parameterAnnotation = parameterAnnotations[i];

            for (Annotation annotation : parameterAnnotation) {
                if (annotation.annotationType().equals(BsonProperty.class)) {
                    properties.add((BsonProperty) annotation);
                    break;
                }

                if (annotation.annotationType().equals(BsonId.class)) {
                    properties.add(null);
                    idPropertyIndex = i;
                    break;
                }
            }
        }

        this.idPropertyIndex = idPropertyIndex;
    }

    Class<T> getType() {
        return clazz;
    }

    List<BsonProperty> getProperties() {
        return properties;
    }

    Integer getIdPropertyIndex() {
        return idPropertyIndex;
    }

    List<Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    List<Type> getParameterGenericTypes() {
        return parameterGenericTypes;
    }

    T getInstance() {
        checkHasAnExecutable();
        try {
            return invoker.create();
        } catch (Exception e) {
            throw new CodecConfigurationException(e.getMessage(), e);
        }
    }

    T getInstance(final Object[] params) {
        checkHasAnExecutable();
        try {
            return invoker.create(parameterTypes.stream().toArray((IntFunction<Class<?>[]>) Class[]::new), params);
        } catch (Exception e) {
            throw new CodecConfigurationException(e.getMessage(), e);
        }
    }

    private void checkHasAnExecutable() {
        if (invoker == null) {
            throw new CodecConfigurationException(format("Cannot find a public constructor for '%s'.", clazz.getSimpleName()));
        }
    }

    CodecConfigurationException getError(final Class<?> clazz, final String msg) {
        return new CodecConfigurationException(format("Invalid @BsonCreator constructor or method in %s. %s",
                clazz.getSimpleName(), msg));
    }

}
