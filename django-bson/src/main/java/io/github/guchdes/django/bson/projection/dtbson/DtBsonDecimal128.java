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

import org.bson.BsonNumber;
import org.bson.BsonType;
import org.bson.types.Decimal128;

import static org.bson.assertions.Assertions.notNull;

/**
 * A representation of the BSON Decimal128 type.
 *
 * @since 3.4
 */
public final class DtBsonDecimal128 extends DtBsonNumber {
    private final Decimal128 value;

    /**
     * Construct a new instance with the given value.
     *
     * @param value the value, which may not be null
     */
    public DtBsonDecimal128(final Decimal128 value) {
        notNull("value", value);
        this.value = value;
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.DECIMAL128;
    }

    /**
     * Gets the Decimal128 value.
     *
     * @return the value
     */
    public Decimal128 getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonDecimal128 that = (DtBsonDecimal128) o;

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
        return "DtBsonDecimal128{"
                       + "value=" + value
                       + '}';
    }

    @Override
    public int intValue() {
        return value.bigDecimalValue().intValue();
    }

    @Override
    public long longValue() {
        return value.bigDecimalValue().longValue();
    }

    @Override
    public double doubleValue() {
        return value.bigDecimalValue().doubleValue();
    }

    @Override
    public Decimal128 decimal128Value() {
        return value;
    }
}
