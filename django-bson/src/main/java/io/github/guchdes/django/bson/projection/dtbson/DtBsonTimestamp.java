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
 * A value representing the BSON timestamp type.
 *
 * @since 3.0
 */
public final class DtBsonTimestamp implements DtBsonValue, Comparable<DtBsonTimestamp> {

    private final long value;

    /**
     * Construct a new instance with a null time and a 0 increment.
     */
    public DtBsonTimestamp() {
        value = 0;
    }

    /**
     * Construct a new instance for the given value, which combines the time in seconds and the increment as a single long value.
     *
     * @param value the timetamp as a single long value
     * @since 3.5
     */
    public DtBsonTimestamp(final long value) {
        this.value = value;
    }

    /**
     * Construct a new instance for the given time and increment.
     *
     * @param seconds   the number of seconds since the epoch
     * @param increment the increment.
     */
    public DtBsonTimestamp(final int seconds, final int increment) {
        value = ((long) seconds << 32) | (increment & 0xFFFFFFFFL);
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.TIMESTAMP;
    }


    /**
     * Gets the value of the timestamp.
     *
     * @return the timestamp value
     * @since 3.5
     */
    public long getValue() {
        return value;
    }

    /**
     * Gets the time in seconds since epoch.
     *
     * @return an int representing time in seconds since epoch
     */
    public int getTime() {
        return (int) (value >> 32);
    }

    /**
     * Gets the increment value.
     *
     * @return an incrementing ordinal for operations within a given second
     */
    public int getInc() {
        return (int) value;
    }

    @Override
    public String toString() {
        return "DtBsonTimestamp{"
                + "value=" + getValue()
                + ", seconds=" + getTime()
                + ", inc=" + getInc()
                + '}';
    }

    @Override
    public int compareTo(final DtBsonTimestamp ts) {
        return Long.compareUnsigned(value, ts.value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonTimestamp timestamp = (DtBsonTimestamp) o;

        if (value != timestamp.value) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }
}
