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

import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.types.ObjectId;

/**
 * A representation of the BSON ObjectId type.
 */
public class DtBsonObjectId implements DtBsonValue, Comparable<DtBsonObjectId> {

    private final ObjectId value;

    /**
     * Construct a new instance with a new {@code ObjectId}.
     */
    public DtBsonObjectId() {
        this(new ObjectId());
    }

    /**
     * Construct a new instance with the given {@code ObjectId} instance.
     *
     * @param value the ObjectId
     */
    public DtBsonObjectId(final ObjectId value) {
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }
        this.value = value;
    }

    /**
     * Get the {@code ObjectId} value.
     *
     * @return the {@code ObjectId} value
     */
    public ObjectId getValue() {
        return value;
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.OBJECT_ID;
    }

    @Override
    public int compareTo(final DtBsonObjectId o) {
        return value.compareTo(o.value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonObjectId that = (DtBsonObjectId) o;

        if (!value.equals(that.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "DtBsonObjectId{"
                + "value=" + value.toHexString()
                + '}';
    }
}
