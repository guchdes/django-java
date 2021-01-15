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

import java.math.BigDecimal;

/**
 * A representation of the BSON Double type.
 *
 * @since 3.0
 */
public class DtBsonDouble extends DtBsonNumber implements Comparable<DtBsonDouble> {

    private final double value;

    /**
     * Construct a new instance with the given value.
     *
     * @param value the value
     */
    public DtBsonDouble(final double value) {
        this.value = value;
    }

    @Override
    public int compareTo(final DtBsonDouble o) {
        return Double.compare(value, o.value);
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.DOUBLE;
    }

    /**
     * Gets the double value.
     *
     * @return the value
     */
    public double getValue() {
        return value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public Decimal128 decimal128Value() {
        if (Double.isNaN(value)) {
            return Decimal128.NaN;
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? Decimal128.POSITIVE_INFINITY : Decimal128.NEGATIVE_INFINITY;
        }

        return new Decimal128(new BigDecimal(value));
    }

    @Override
    public double doubleValue() {
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

        DtBsonDouble that = (DtBsonDouble) o;

        if (Double.compare(that.value, value) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return "DtBsonDouble{"
               + "value=" + value
               + '}';
    }
}
