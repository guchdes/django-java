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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.*;

public final class PropertyMetadata<T> {
    private final String name;
    private final String declaringClassName;
    private final TypeData<T> typeData;
    private final Type genericType;
    private final Map<Class<? extends Annotation>, Annotation> readAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
    private final Map<Class<? extends Annotation>, Annotation> writeAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
    private TypeParameterMap typeParameterMap;
    private List<TypeData<?>> typeParameters;

    private String error;
    private Field field;
    private Method getter;
    private Method setter;

    PropertyMetadata(final String name, final String declaringClassName, final TypeData<T> typeData, final Type genericType) {
        this.name = name;
        this.declaringClassName = declaringClassName;
        this.typeData = typeData;
        this.genericType = genericType;
    }

    public String getName() {
        return name;
    }

    public Type getGenericType() {
        return genericType;
    }

    public List<Annotation> getReadAnnotations() {
        return new ArrayList<Annotation>(readAnnotations.values());
    }

    public PropertyMetadata<T> addReadAnnotation(final Annotation annotation) {
        if (readAnnotations.containsKey(annotation.annotationType())) {
            if (annotation.equals(readAnnotations.get(annotation.annotationType()))) {
                return this;
            }
            throw new CodecConfigurationException(format("Read annotation %s for '%s' already exists in %s", annotation.annotationType(),
                    name, declaringClassName));
        }
        readAnnotations.put(annotation.annotationType(), annotation);
        return this;
    }

    public List<Annotation> getWriteAnnotations() {
        return new ArrayList<Annotation>(writeAnnotations.values());
    }

    public PropertyMetadata<T> addWriteAnnotation(final Annotation annotation) {
        if (writeAnnotations.containsKey(annotation.annotationType())) {
            if (annotation.equals(writeAnnotations.get(annotation.annotationType()))) {
                return this;
            }
            throw new CodecConfigurationException(format("Write annotation %s for '%s' already exists in %s", annotation.annotationType(),
                    name, declaringClassName));
        }
        writeAnnotations.put(annotation.annotationType(), annotation);
        return this;
    }

    public Field getField() {
        return field;
    }

    public PropertyMetadata<T> field(final Field field) {
        this.field = field;
        return this;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(final Method getter) {
        this.getter = getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(final Method setter) {
        this.setter = setter;
    }

    public String getDeclaringClassName() {
        return declaringClassName;
    }

    public TypeData<T> getTypeData() {
        return typeData;
    }

    public TypeParameterMap getTypeParameterMap() {
        return typeParameterMap;
    }

    public List<TypeData<?>> getTypeParameters() {
        return typeParameters;
    }

    public <S> PropertyMetadata<T> typeParameterInfo(final TypeParameterMap typeParameterMap, final TypeData<S> parentTypeData) {
        if (typeParameterMap != null && parentTypeData != null) {
            this.typeParameterMap = typeParameterMap;
            this.typeParameters = parentTypeData.getTypeParameters();
        }
        return this;
    }

    String getError() {
        return error;
    }

    void setError(final String error) {
        this.error = error;
    }

    public boolean isSerializable() {
        if (getter != null) {
            return field == null || notStaticOrTransient(field.getModifiers());
        } else {
            return field != null && isPublicAndNotStaticOrTransient(field.getModifiers());
        }
    }

    public boolean isDeserializable() {
        if (setter != null) {
            return field == null || !isFinal(field.getModifiers()) && notStaticOrTransient(field.getModifiers());
        } else {
            return field != null && !isFinal(field.getModifiers()) && isPublicAndNotStaticOrTransient(field.getModifiers());
        }
    }

    private boolean notStaticOrTransient(final int modifiers) {
        return !(isTransient(modifiers) || isStatic(modifiers));
    }

    private boolean isPublicAndNotStaticOrTransient(final int modifiers) {
        return isPublic(modifiers) && notStaticOrTransient(modifiers);
    }
}
