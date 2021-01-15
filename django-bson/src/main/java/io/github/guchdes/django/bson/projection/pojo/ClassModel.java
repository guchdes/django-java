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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This model represents the metadata for a class and all its properties.
 *
 * @param <T> The type of the class the ClassModel represents
 * @since 3.5
 */
public final class ClassModel<T> {
    private final String name;
    private final Class<T> type;
    private final boolean hasTypeParameters;
    private final InstanceCreatorFactory<T> instanceCreatorFactory;
    private final boolean discriminatorEnabled;
    private final String discriminatorKey;
    private final String discriminator;
    private final IdPropertyModelHolder<?> idPropertyModelHolder;
    private final List<PropertyModel<?>> propertyModels;
    private final Map<String, TypeParameterMap> propertyNameToTypeParameterMap;

    ClassModel(final Class<T> clazz, final Map<String, TypeParameterMap> propertyNameToTypeParameterMap,
               final InstanceCreatorFactory<T> instanceCreatorFactory, final Boolean discriminatorEnabled, final String discriminatorKey,
               final String discriminator, final IdPropertyModelHolder<?> idPropertyModelHolder,
               final List<PropertyModel<?>> propertyModels) {
        this.name = clazz.getSimpleName();
        this.type = clazz;
        this.hasTypeParameters = clazz.getTypeParameters().length > 0;
        this.propertyNameToTypeParameterMap = Collections.unmodifiableMap(propertyNameToTypeParameterMap);
        this.instanceCreatorFactory = instanceCreatorFactory;
        this.discriminatorEnabled = discriminatorEnabled;
        this.discriminatorKey = discriminatorKey;
        this.discriminator = discriminator;
        this.idPropertyModelHolder = idPropertyModelHolder;
        this.propertyModels = Collections.unmodifiableList(propertyModels);
    }

    /**
     * Creates a new Class Model builder instance using reflection.
     *
     * @param type the POJO class to reflect and configure the builder with.
     * @param <S> the type of the class
     * @return a new Class Model builder instance using reflection on the {@code clazz}.
     */
    public static <S> ClassModelBuilder<S> builder(final Class<S> type) {
        return new ClassModelBuilder<S>(type);
    }

    /**
     * @return a new InstanceCreator instance for the ClassModel
     */
    InstanceCreator<T> getInstanceCreator() {
        return instanceCreatorFactory.create();
    }

    /**
     * @return the backing class for the ClassModel
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @return true if the underlying type has type parameters.
     */
    public boolean hasTypeParameters() {
        return hasTypeParameters;
    }

    /**
     * @return true if a discriminator should be used when storing the data.
     */
    public boolean useDiscriminator() {
        return discriminatorEnabled;
    }

    /**
     * Gets the value for the discriminator.
     *
     * @return the discriminator value or null if not set
     */
    public String getDiscriminatorKey() {
        return discriminatorKey;
    }

    /**
     * Returns the discriminator key.
     *
     * @return the discriminator key or null if not set
     */
    public String getDiscriminator() {
        return discriminator;
    }

    /**
     * Gets a {@link PropertyModel} by the property name.
     *
     * @param propertyName the PropertyModel's property name
     * @return the PropertyModel or null if the property is not found
     */
    public PropertyModel<?> getPropertyModel(final String propertyName) {
        for (PropertyModel<?> propertyModel : propertyModels) {
            if (propertyModel.getName().equals(propertyName)) {
                return propertyModel;
            }
        }
        return null;
    }

    /**
     * Returns all the properties on this model
     *
     * @return the list of properties
     */
    public List<PropertyModel<?>> getPropertyModels() {
        return propertyModels;
    }

    /**
     * Returns the {@link PropertyModel} mapped as the id property for this ClassModel
     *
     * @return the PropertyModel for the id
     */
    public PropertyModel<?> getIdPropertyModel() {
        return idPropertyModelHolder != null ? idPropertyModelHolder.getPropertyModel() : null;
    }

    IdPropertyModelHolder<?> getIdPropertyModelHolder() {
        return idPropertyModelHolder;
    }

    /**
     * Returns the name of the class represented by this ClassModel
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ClassModel{"
                + "type=" + type
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

        ClassModel<?> that = (ClassModel<?>) o;

        if (discriminatorEnabled != that.discriminatorEnabled) {
            return false;
        }
        if (!getType().equals(that.getType())) {
            return false;
        }
        if (!getInstanceCreatorFactory().equals(that.getInstanceCreatorFactory())) {
            return false;
        }
        if (getDiscriminatorKey() != null ? !getDiscriminatorKey().equals(that.getDiscriminatorKey())
                : that.getDiscriminatorKey() != null) {
            return false;
        }
        if (getDiscriminator() != null ? !getDiscriminator().equals(that.getDiscriminator()) : that.getDiscriminator() != null) {
            return false;
        }
        if (idPropertyModelHolder != null ? !idPropertyModelHolder.equals(that.idPropertyModelHolder)
                : that.idPropertyModelHolder != null) {
            return false;
        }
        if (!getPropertyModels().equals(that.getPropertyModels())) {
            return false;
        }
        if (!getPropertyNameToTypeParameterMap().equals(that.getPropertyNameToTypeParameterMap())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getInstanceCreatorFactory().hashCode();
        result = 31 * result + (discriminatorEnabled ? 1 : 0);
        result = 31 * result + (getDiscriminatorKey() != null ? getDiscriminatorKey().hashCode() : 0);
        result = 31 * result + (getDiscriminator() != null ? getDiscriminator().hashCode() : 0);
        result = 31 * result + (getIdPropertyModelHolder() != null ? getIdPropertyModelHolder().hashCode() : 0);
        result = 31 * result + getPropertyModels().hashCode();
        result = 31 * result + getPropertyNameToTypeParameterMap().hashCode();
        return result;
    }

    public InstanceCreatorFactory<T> getInstanceCreatorFactory() {
        return instanceCreatorFactory;
    }

    Map<String, TypeParameterMap> getPropertyNameToTypeParameterMap() {
        return propertyNameToTypeParameterMap;
    }

}
