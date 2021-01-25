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
import static java.lang.String.format;

final class PropertyAccessorImpl<T> implements PropertyAccessor<T> {

    private final PropertyMetadata<T> propertyMetadata;

    PropertyAccessorImpl(final PropertyMetadata<T> propertyMetadata) {
        this.propertyMetadata = propertyMetadata;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> T get(final S instance) {
        try {
            if (propertyMetadata.isSerializable()) {
                if (propertyMetadata.getGetter() != null) {
                    return (T) propertyMetadata.getGetter().invoke(instance);
                } else {
                    return (T) propertyMetadata.getField().get(instance);
                }
            } else {
                throw getError(null);
            }
        } catch (final Exception e) {
            throw getError(e);
        }
    }

    @Override
    public <S> void set(final S instance, final T value) {
        try {
            if (propertyMetadata.isDeserializable()) {
                if (propertyMetadata.getSetter() != null) {
                    propertyMetadata.getSetter().invoke(instance, value);
                } else {
                    propertyMetadata.getField().set(instance, value);
                }
            }
        } catch (final Exception e) {
            throw setError(e);
        }
    }

    PropertyMetadata<T> getPropertyMetadata() {
        return propertyMetadata;
    }

    private CodecConfigurationException getError(final Exception cause) {
        return new CodecConfigurationException(format("Unable to get value for property '%s' in %s", propertyMetadata.getName(),
                propertyMetadata.getDeclaringClassName()), cause);
    }

    private CodecConfigurationException setError(final Exception cause) {
        return new CodecConfigurationException(format("Unable to set value for property '%s' in %s", propertyMetadata.getName(),
                propertyMetadata.getDeclaringClassName()), cause);
    }
}
