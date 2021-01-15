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
/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.guchdes.django.bson.projection.pojo;

import org.bson.codecs.configuration.CodecConfigurationException;
import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;

final class ConventionUseGettersAsSettersImpl implements Convention {

    @Override
    public void apply(final ClassModelBuilder<?> classModelBuilder) {
        for (PropertyModelBuilder<?> propertyModelBuilder : classModelBuilder.getPropertyModelBuilders()) {
            if (!(propertyModelBuilder.getPropertyAccessor() instanceof PropertyAccessorImpl)) {
                throw new CodecConfigurationException(format("The USE_GETTER_AS_SETTER_CONVENTION is not compatible with "
                        + "propertyModelBuilder instance that have custom implementations of PropertyAccessor: %s",
                        propertyModelBuilder.getPropertyAccessor().getClass().getName()));
            }
            PropertyAccessorImpl<?> defaultAccessor = (PropertyAccessorImpl<?>) propertyModelBuilder.getPropertyAccessor();
            PropertyMetadata<?> propertyMetaData = defaultAccessor.getPropertyMetadata();
            if (!propertyMetaData.isDeserializable() && propertyMetaData.isSerializable()
                    && isMapOrCollection(propertyMetaData.getTypeData().getType())) {
                setPropertyAccessor(propertyModelBuilder);
            }
        }
    }

    private <T> boolean isMapOrCollection(final Class<T> clazz) {
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> void setPropertyAccessor(final PropertyModelBuilder<T> propertyModelBuilder) {
        propertyModelBuilder.propertyAccessor(new PrivatePropertyAccessor<T>(
                (PropertyAccessorImpl<T>) propertyModelBuilder.getPropertyAccessor()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class PrivatePropertyAccessor<T> implements PropertyAccessor<T> {
        private final PropertyAccessorImpl<T> wrapped;

        private PrivatePropertyAccessor(final PropertyAccessorImpl<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public <S> T get(final S instance) {
            return wrapped.get(instance);
        }

        @Override
        public <S> void set(final S instance, final T value) {
            if (value instanceof Collection) {
                mutateCollection(instance, (Collection) value);
            } else if (value instanceof Map) {
                mutateMap(instance, (Map) value);
            } else {
                throwCodecConfigurationException(format("Unexpected type: '%s'", value.getClass()), null);
            }
        }

        private <S> void mutateCollection(final S instance, final Collection value) {
            T originalCollection = get(instance);
            Collection<?> collection = ((Collection<?>) originalCollection);
            if (collection == null) {
                throwCodecConfigurationException("The getter returned null.", null);
            } else if (!collection.isEmpty()) {
                throwCodecConfigurationException("The getter returned a non empty collection.", null);
            } else {
                try {
                    collection.addAll(value);
                } catch (Exception e) {
                    throwCodecConfigurationException("collection#addAll failed.", e);
                }
            }
        }

        private <S> void mutateMap(final S instance, final Map value) {
            T originalMap = get(instance);
            Map<?, ?> map = ((Map<?, ?>) originalMap);
            if (map == null) {
                throwCodecConfigurationException("The getter returned null.", null);
            } else if (!map.isEmpty()) {
                throwCodecConfigurationException("The getter returned a non empty map.", null);
            } else {
                try {
                    map.putAll(value);
                } catch (Exception e) {
                    throwCodecConfigurationException("map#putAll failed.", e);
                }
            }
        }
        private void throwCodecConfigurationException(final String reason, final Exception cause) {
            throw new CodecConfigurationException(format("Cannot use getter in '%s' to set '%s'. %s",
                    wrapped.getPropertyMetadata().getDeclaringClassName(), wrapped.getPropertyMetadata().getName(), reason), cause);
        }
    }
}
