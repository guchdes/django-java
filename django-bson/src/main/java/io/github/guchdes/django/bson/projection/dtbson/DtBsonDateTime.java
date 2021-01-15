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

/**
 * A representation of the BSON DateTime type.
 *
 * @since 3.0
 */
public class DtBsonDateTime implements DtBsonValue, Comparable<DtBsonDateTime> {

    private final long value;

    /**
     * Construct a new instance with the given value.
     *
     * @param value the value, which may not be null
     */
    public DtBsonDateTime(final long value) {
        this.value = value;
    }

    @Override
    public int compareTo(final DtBsonDateTime o) {
        return Long.valueOf(value).compareTo(o.value);
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.DATE_TIME;
    }

    /**
     * Gets the DateTime value as a long
     *
     * @return the value
     */
    public long getValue() {
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

        DtBsonDateTime that = (DtBsonDateTime) o;

        if (value != that.value) {
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
        return "DtBsonDateTime{"
                + "value=" + value
                + '}';
    }
}
