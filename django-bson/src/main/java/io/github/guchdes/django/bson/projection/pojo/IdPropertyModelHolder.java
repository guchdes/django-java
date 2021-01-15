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

import org.bson.codecs.configuration.CodecConfigurationException;
import static java.lang.String.format;

final class IdPropertyModelHolder<I> {
    private final PropertyModel<I> propertyModel;
    private final IdGenerator<I> idGenerator;

    static <T, I> IdPropertyModelHolder<I> create(final ClassModel<T> classModel, final PropertyModel<I> idPropertyModel) {
        return create(classModel.getType(), idPropertyModel, classModel.getIdPropertyModelHolder().getIdGenerator());
    }

    @SuppressWarnings("unchecked")
    static <T, I, V> IdPropertyModelHolder<I> create(final Class<T> type, final PropertyModel<I> idProperty,
                                                                          final IdGenerator<V> idGenerator) {
        if (idProperty == null && idGenerator != null) {
            throw new CodecConfigurationException(format("Invalid IdGenerator. There is no IdProperty set for: %s", type));
        } else if (idGenerator != null && !idProperty.getTypeData().getType().isAssignableFrom(idGenerator.getType())) {
            throw new CodecConfigurationException(format("Invalid IdGenerator. Mismatching types, the IdProperty type is: %s but"
                    + " the IdGenerator type is: %s", idProperty.getTypeData().getType(), idGenerator.getType()));
        }
        return new IdPropertyModelHolder<I>(idProperty, (IdGenerator<I>) idGenerator);
    }

    private IdPropertyModelHolder(final PropertyModel<I> propertyModel, final IdGenerator<I> idGenerator) {
        this.propertyModel = propertyModel;
        this.idGenerator = idGenerator;
    }

    PropertyModel<I> getPropertyModel() {
        return propertyModel;
    }

    IdGenerator<I> getIdGenerator() {
        return idGenerator;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IdPropertyModelHolder<?> that = (IdPropertyModelHolder<?>) o;

        if (propertyModel != null ? !propertyModel.equals(that.propertyModel) : that.propertyModel != null) {
            return false;
        }
        return idGenerator != null ? idGenerator.equals(that.idGenerator) : that.idGenerator == null;
    }

    @Override
    public int hashCode() {
        int result = propertyModel != null ? propertyModel.hashCode() : 0;
        result = 31 * result + (idGenerator != null ? idGenerator.hashCode() : 0);
        return result;
    }
}
