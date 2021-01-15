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
package io.github.guchdes.django.bson.projection.dtbson;

import org.bson.*;
import org.bson.assertions.Assertions;
import org.bson.internal.UuidHelper;

import java.util.Arrays;
import java.util.UUID;

/**
 * A representation of the BSON Binary type.  Note that for performance reasons instances of this class are not immutable,
 * so care should be taken to only modify the underlying byte array if you know what you're doing, or else make a defensive copy.
 *
 * @since 3.0
 */
public class DtBsonBinary implements DtBsonValue {

    private final byte type;
    private final byte[] data;

    /**
     * Construct a new instance with the given data and the default sub-type
     *
     * @param data the data
     *
     * @see BsonBinarySubType#BINARY
     */
    public DtBsonBinary(final byte[] data) {
        this(BsonBinarySubType.BINARY, data);
    }

    /**
     * Construct a new instance with the given data and binary sub type.
     *
     * @param data the data
     * @param type the binary sub type
     *
     * @see BsonBinarySubType#BINARY
     */
    public DtBsonBinary(final BsonBinarySubType type, final byte[] data) {
        if (type == null) {
            throw new IllegalArgumentException("type may not be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        }
        this.type = type.getValue();
        this.data = data;
    }

    /**
     * Construct a new instance with the given data and binary sub type.
     *
     * @param data the data
     * @param type the binary sub type
     *
     * @see BsonBinarySubType#BINARY
     */
    public DtBsonBinary(final byte type, final byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        }
        this.type = type;
        this.data = data;
    }

    /**
     * Construct a Type 4 BsonBinary from the given UUID.
     *
     * @param uuid the UUID
     * @since 3.9
     */
    public DtBsonBinary(final UUID uuid) {
        this(uuid, UuidRepresentation.STANDARD);
    }

    /**
     * Construct a new instance from the given UUID and UuidRepresentation
     *
     * @param uuid the UUID
     * @param uuidRepresentation the UUID representation
     * @since 3.9
     */
    public DtBsonBinary(final UUID uuid, final UuidRepresentation uuidRepresentation) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid may not be null");
        }
        if (uuidRepresentation == null) {
            throw new IllegalArgumentException("uuidRepresentation may not be null");
        }
        this.data = UuidHelper.encodeUuidToBinary(uuid, uuidRepresentation);
        this.type = uuidRepresentation == UuidRepresentation.STANDARD
                ? BsonBinarySubType.UUID_STANDARD.getValue()
                : BsonBinarySubType.UUID_LEGACY.getValue();
    }

    /**
     * Returns the binary as a UUID. The binary type must be 4.
     *
     * @return the uuid
     * @since 3.9
     */
    public UUID asUuid() {
        if (!BsonBinarySubType.isUuid(type)) {
            throw new BsonInvalidOperationException("type must be a UUID subtype.");
        }

        if (type != BsonBinarySubType.UUID_STANDARD.getValue()) {
            throw new BsonInvalidOperationException("uuidRepresentation must be set to return the correct UUID.");
        }

        return UuidHelper.decodeBinaryToUuid(this.data.clone(), this.type, UuidRepresentation.STANDARD);
    }

    /**
     * Returns the binary as a UUID.
     *
     * @param uuidRepresentation the UUID representation
     * @return the uuid
     * @since 3.9
     */
    public UUID asUuid(final UuidRepresentation uuidRepresentation) {
        Assertions.notNull("uuidRepresentation", uuidRepresentation);

        final byte uuidType = uuidRepresentation == UuidRepresentation.STANDARD
                ? BsonBinarySubType.UUID_STANDARD.getValue()
                : BsonBinarySubType.UUID_LEGACY.getValue();

        if (type != uuidType) {
            throw new BsonInvalidOperationException("uuidRepresentation does not match current uuidRepresentation.");
        }

        return UuidHelper.decodeBinaryToUuid(data.clone(), type, uuidRepresentation);
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.BINARY;
    }

    /**
     * Gets the type of this Binary.
     *
     * @return the type
     */
    public byte getType() {
        return type;
    }

    /**
     * Gets the data of this Binary.
     *
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonBinary that = (DtBsonBinary) o;

        if (!Arrays.equals(data, that.data)) {
            return false;
        }
        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) type;
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "DtBsonBinary{"
               + "type=" + type
               + ", data=" + Arrays.toString(data)
               + '}';
    }

    static DtBsonBinary clone(final DtBsonBinary from) {
        return new DtBsonBinary(from.type, from.data.clone());
    }
}
