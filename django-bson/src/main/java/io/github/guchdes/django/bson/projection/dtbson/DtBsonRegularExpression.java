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

import java.util.Arrays;

import static org.bson.assertions.Assertions.notNull;

/**
 * A holder class for a BSON regular expression, so that we can delay compiling into a Pattern until necessary.
 *
 * @since 3.0
 */
public final class DtBsonRegularExpression implements DtBsonValue {

    private final String pattern;
    private final String options;

    /**
     * Creates a new instance
     *
     * @param pattern the regular expression {@link java.util.regex.Pattern}
     * @param options the options for the regular expression
     */
    public DtBsonRegularExpression(final String pattern, final String options) {
        this.pattern = notNull("pattern", pattern);
        this.options = options == null ? "" : sortOptionCharacters(options);
    }

    /**
     * Creates a new instance with no options set.
     *
     * @param pattern the regular expression {@link java.util.regex.Pattern}
     */
    public DtBsonRegularExpression(final String pattern) {
        this(pattern, null);
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.REGULAR_EXPRESSION;
    }

    /**
     * Gets the regex pattern.
     *
     * @return the regular expression pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Gets the options for the regular expression
     *
     * @return the options.
     */
    public String getOptions() {
        return options;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonRegularExpression that = (DtBsonRegularExpression) o;

        if (!options.equals(that.options)) {
            return false;
        }
        if (!pattern.equals(that.pattern)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pattern.hashCode();
        result = 31 * result + options.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DtBsonRegularExpression{"
               + "pattern='" + pattern + '\''
               + ", options='" + options + '\''
               + '}';
    }

    private String sortOptionCharacters(final String options) {
        char[] chars = options.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }
}
