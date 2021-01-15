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

import io.github.guchdes.django.bson.projection.dtbson.DtBsonNull;
import io.github.guchdes.django.bson.projection.dtbson.DtBsonValue;
import org.bson.codecs.Codec;

import java.lang.reflect.Type;

/**
 * Represents a property on a class and stores various metadata such as generic parameters
 *
 * @param <T> the type of the property that the PropertyModel represents.
 * @since 3.5
 */
public final class PropertyModel<T> {
    private final String name;
    private final String readName;
    private final String writeName;
    private final TypeData<T> typeData;
    private final Codec<T> codec;
    private final PropertySerialization<T> propertySerialization;
    private final Boolean useDiscriminator;
    private final PropertyAccessor<T> propertyAccessor;
    private final PropertyMetadata<T> propertyMetadata;
    private final String error;
    private volatile Codec<T> cachedCodec;

    PropertyModel(final String name, final String readName, final String writeName, final TypeData<T> typeData,
                  final Codec<T> codec, final PropertySerialization<T> propertySerialization, final Boolean useDiscriminator,
                  final PropertyMetadata<T> propertyMetadata, final PropertyAccessor<T> propertyAccessor, final String error) {
        this.name = name;
        this.readName = readName;
        this.writeName = writeName;
        this.typeData = typeData;
        this.codec = codec;
        this.cachedCodec = codec;
        this.propertySerialization = propertySerialization;
        this.useDiscriminator = useDiscriminator;
        this.propertyAccessor = propertyAccessor;
        this.propertyMetadata = propertyMetadata;
        this.error = error;
    }

    /**
     * Create a new {@link PropertyModelBuilder}
     * @param <T> the type of the property
     * @return the builder
     */
    public static <T> PropertyModelBuilder<T> builder() {
        return new PropertyModelBuilder<T>();
    }

    /**
     * @return the property name for the model
     */
    public String getName() {
        return name;
    }

    /**
     * @return the name of the property to use as the key when deserializing from BSON
     */
    public String getWriteName() {
        return writeName;
    }

    /**
     * @return the name of the property to use as the key when serializing into BSON
     */
    public String getReadName() {
        return readName;
    }

    /**
     * Property is writable.
     *
     * @return true if can be deserialized from BSON
     */
    public boolean isWritable() {
        return writeName != null;
    }

    /**
     * Property is readable.
     *
     * @return true if can be serialized to BSON
     */
    public boolean isReadable() {
        return readName != null;
    }

    /**
     * @return the type data for the property
     */
    public TypeData<T> getTypeData() {
        return typeData;
    }

    /**
     * @return the generic type for the property
     */
    public Type getGenericType() {
        return TypeWithTypeParametersUtils.toGenericType(typeData);
    }

    /**
     * @return the custom codec to use if set or null
     */
    public Codec<T> getCodec() {
        return codec;
    }

    /**
     * Returns true if the value should be serialized.
     *
     * @param value the value to check
     * @return true if the value should be serialized.
     */
    public boolean shouldSerialize(final T value) {
        return propertySerialization.shouldSerialize(value);
    }

    /**
     * @return the property accessor
     */
    public PropertyAccessor<T> getPropertyAccessor() {
        return propertyAccessor;
    }

    /**
     * @return the property metadata
     */
    public PropertyMetadata<T> getPropertyMetadata() {
        return propertyMetadata;
    }

    /**
     * @return true or false if a discriminator should be used when serializing or null if not set
     */
    public Boolean useDiscriminator() {
        return useDiscriminator;
    }

    public boolean shouldDecodeBsonNull() {
        return getTypeData().getType() == DtBsonValue.class || getTypeData().getType() == DtBsonNull.class;
    }

    @Override
    public String toString() {
        return "PropertyModel{"
                + "propertyName='" + name + "'"
                + ", readName='" + readName + "'"
                + ", writeName='" + writeName + "'"
                + ", typeData=" + typeData
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

        PropertyModel<?> that = (PropertyModel<?>) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }
        if (getReadName() != null ? !getReadName().equals(that.getReadName()) : that.getReadName() != null) {
            return false;
        }
        if (getWriteName() != null ? !getWriteName().equals(that.getWriteName()) : that.getWriteName() != null) {
            return false;
        }
        if (getTypeData() != null ? !getTypeData().equals(that.getTypeData()) : that.getTypeData() != null) {
            return false;
        }
        if (getCodec() != null ? !getCodec().equals(that.getCodec()) : that.getCodec() != null) {
            return false;
        }
        if (getPropertyMetadata() != null ? !getPropertyMetadata().equals(that.getPropertyMetadata()) : that.getPropertyMetadata() != null) {
            return false;
        }
        if (getPropertySerialization() != null ? !getPropertySerialization().equals(that.getPropertySerialization()) : that
                .getPropertySerialization() != null) {
            return false;
        }
        if (useDiscriminator != null ? !useDiscriminator.equals(that.useDiscriminator) : that.useDiscriminator != null) {
            return false;
        }
        if (getPropertyAccessor() != null ? !getPropertyAccessor().equals(that.getPropertyAccessor())
                : that.getPropertyAccessor() != null) {
            return false;
        }

        if (getError() != null ? !getError().equals(that.getError()) : that.getError() != null) {
            return false;
        }

        if (getCachedCodec() != null ? !getCachedCodec().equals(that.getCachedCodec()) : that.getCachedCodec() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getReadName() != null ? getReadName().hashCode() : 0);
        result = 31 * result + (getWriteName() != null ? getWriteName().hashCode() : 0);
        result = 31 * result + (getTypeData() != null ? getTypeData().hashCode() : 0);
        result = 31 * result + (getCodec() != null ? getCodec().hashCode() : 0);
        result = 31 * result + (getPropertySerialization() != null ? getPropertySerialization().hashCode() : 0);
        result = 31 * result + (useDiscriminator != null ? useDiscriminator.hashCode() : 0);
        result = 31 * result + (getPropertyAccessor() != null ? getPropertyAccessor().hashCode() : 0);
        result = 31 * result + (getPropertyMetadata() != null ? getPropertyMetadata().hashCode() : 0);
        result = 31 * result + (getError() != null ? getError().hashCode() : 0);
        result = 31 * result + (getCachedCodec() != null ? getCachedCodec().hashCode() : 0);
        return result;
    }

    boolean hasError() {
        return error != null;
    }

    String getError() {
        return error;
    }

    PropertySerialization<T> getPropertySerialization() {
        return propertySerialization;
    }

    void cachedCodec(final Codec<T> codec) {
        this.cachedCodec = codec;
    }

    Codec<T> getCachedCodec() {
        return cachedCodec;
    }
}
