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
package com.mountsea.django.bson.projection.dtbson;

import org.bson.BsonType;
import org.bson.types.Decimal128;

/**
 * A representation of the BSON Int64 type.
 */
public final class DtBsonInt64 extends DtBsonNumber implements Comparable<DtBsonInt64> {

    private final long value;

    /**
     * Construct a new instance with the given value.
     *
     * @param value the value
     */
    public DtBsonInt64(final long value) {
        this.value = value;
    }

    @Override
    public int compareTo(final DtBsonInt64 o) {
        return (value < o.value) ? -1 : ((value == o.value) ? 0 : 1);
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.INT64;
    }


    /**
     * Gets the long value.
     *
     * @return the value
     */
    public long getValue() {
        return value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public Decimal128 decimal128Value() {
        return new Decimal128(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonInt64 bsonInt64 = (DtBsonInt64) o;

        if (value != bsonInt64.value) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public String toString() {
        return "DtBsonInt64{"
               + "value=" + value
               + '}';
    }
}
