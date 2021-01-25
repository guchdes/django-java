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

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;


/**
 * Maps the index of a class's generic parameter type index to a property's.
 */
final class TypeParameterMap {
    private final Map<Integer, Integer> propertyToClassParamIndexMap;

    /**
     * Creates a new builder for the TypeParameterMap
     *
     * @return the builder
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a mapping of property type parameter index to the class type parameter index.
     *
     * <p>Note: A property index of -1, means the class's parameter type represents the whole property</p>
     *
     * @return a mapping of property type parameter index to the class type parameter index.
     */
    Map<Integer, Integer> getPropertyToClassParamIndexMap() {
        return propertyToClassParamIndexMap;
    }

    boolean hasTypeParameters() {
        return !propertyToClassParamIndexMap.isEmpty();
    }

    /**
     * A builder for mapping field type parameter indices to the class type parameter indices
     */
    static final class Builder {
        private final Map<Integer, Integer> propertyToClassParamIndexMap = new HashMap<Integer, Integer>();

        private Builder() {
        }

        /**
         * Adds the type parameter index for a class that represents the whole property
         *
         * @param classTypeParameterIndex the class's type parameter index that represents the whole field
         * @return this
         */
        Builder addIndex(final int classTypeParameterIndex) {
            propertyToClassParamIndexMap.put(-1, classTypeParameterIndex);
            return this;
        }

        /**
         * Adds a mapping that represents the property
         *
         * @param propertyTypeParameterIndex the property's type parameter index
         * @param classTypeParameterIndex the class's type parameter index
         * @return this
         */
        Builder addIndex(final int propertyTypeParameterIndex, final int classTypeParameterIndex) {
            propertyToClassParamIndexMap.put(propertyTypeParameterIndex, classTypeParameterIndex);
            return this;
        }

        /**
         * @return the TypeParameterMap
         */
        TypeParameterMap build() {
            if (propertyToClassParamIndexMap.size() > 1 && propertyToClassParamIndexMap.containsKey(-1)) {
                throw new IllegalStateException("You cannot have a generic field that also has type parameters.");
            }
            return new TypeParameterMap(propertyToClassParamIndexMap);
        }
    }

    @Override
    public String toString() {
        return "TypeParameterMap{"
                + "fieldToClassParamIndexMap=" + propertyToClassParamIndexMap
                + "}";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeParameterMap that = (TypeParameterMap) o;

        if (!getPropertyToClassParamIndexMap().equals(that.getPropertyToClassParamIndexMap())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getPropertyToClassParamIndexMap().hashCode();
    }

    private TypeParameterMap(final Map<Integer, Integer> propertyToClassParamIndexMap) {
        this.propertyToClassParamIndexMap = unmodifiableMap(propertyToClassParamIndexMap);
    }
}
