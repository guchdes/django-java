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
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

final class InstanceCreatorImpl<T> implements InstanceCreator<T> {
    private final CreatorExecutable<T> creatorExecutable;
    private final Map<PropertyModel<?>, Object> cachedValues;
    private final Map<String, Integer> properties;
    private final Object[] params;

    private T newInstance;

    InstanceCreatorImpl(final CreatorExecutable<T> creatorExecutable) {
        this.creatorExecutable = creatorExecutable;
        if (creatorExecutable.getProperties().isEmpty()) {
            this.cachedValues = null;
            this.properties = null;
            this.params = null;
            this.newInstance = creatorExecutable.getInstance();
        } else {
            this.cachedValues = new HashMap<PropertyModel<?>, Object>();
            this.properties = new HashMap<String, Integer>();

            for (int i = 0; i < creatorExecutable.getProperties().size(); i++) {
                if (creatorExecutable.getIdPropertyIndex() != null && creatorExecutable.getIdPropertyIndex() == i) {
                    this.properties.put(ClassModelBuilder.ID_PROPERTY_NAME, creatorExecutable.getIdPropertyIndex());
                } else {
                    this.properties.put(creatorExecutable.getProperties().get(i).value(), i);
                }
            }

            this.params = new Object[properties.size()];
        }
    }

    @Override
    public <S> void set(final S value, final PropertyModel<S> propertyModel) {
        if (newInstance != null) {
            propertyModel.getPropertyAccessor().set(newInstance, value);
        } else {
            if (!properties.isEmpty()) {
                String propertyName = propertyModel.getWriteName();

                if (!properties.containsKey(propertyName)) {
                    // Support legacy BsonProperty settings where the property name was used instead of the write name.
                    propertyName = propertyModel.getName();
                }

                Integer index = properties.get(propertyName);
                if (index != null) {
                    params[index] = value;
                }
                properties.remove(propertyName);
            }

            if (properties.isEmpty()) {
                constructInstanceAndProcessCachedValues();
            } else {
                cachedValues.put(propertyModel, value);
            }
        }
    }

    @Override
    public T getInstance() {
        if (newInstance == null) {
            try {
                for (Map.Entry<String, Integer> entry : properties.entrySet()) {
                    params[entry.getValue()] = null;
                }
                constructInstanceAndProcessCachedValues();
            } catch (CodecConfigurationException e) {
                throw new CodecConfigurationException(format("Could not construct new instance of: %s. "
                                + "Missing the following properties: %s",
                        creatorExecutable.getType().getSimpleName(), properties.keySet()), e);
            }
        }
        return newInstance;
    }

    private void constructInstanceAndProcessCachedValues() {
        try {
            newInstance = creatorExecutable.getInstance(params);
        } catch (Exception e) {
            throw new CodecConfigurationException(e.getMessage(), e);
        }

        for (Map.Entry<PropertyModel<?>, Object> entry : cachedValues.entrySet()) {
            setPropertyValue(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private <S> void setPropertyValue(final PropertyModel<S> propertyModel, final Object value) {
        set((S) value, propertyModel);
    }
}
