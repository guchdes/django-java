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
// Symbol.java

package io.github.guchdes.django.bson.projection.dtbson;

import org.bson.BsonType;
import org.bson.BsonValue;

/**
 * Class to hold a BSON symbol object, which is an interned string in Ruby
 *
 * @since 3.0
 */
public class DtBsonSymbol implements DtBsonValue {

    private final String symbol;

    /**
     * Creates a new instance.
     *
     * @param value the symbol value
     */
    public DtBsonSymbol(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can not be null");
        }
        symbol = value;
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.SYMBOL;
    }

    /**
     * Gets the symbol value
     *
     * @return the symbol.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Will compare equal to a String that is equal to the String that this holds
     *
     * @param o the Symbol to compare this to
     * @return true if parameter o is the same as this Symbol
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonSymbol symbol1 = (DtBsonSymbol) o;

        if (!symbol.equals(symbol1.symbol)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return symbol.hashCode();
    }

    @Override
    public String toString() {
        return symbol;
    }
}
