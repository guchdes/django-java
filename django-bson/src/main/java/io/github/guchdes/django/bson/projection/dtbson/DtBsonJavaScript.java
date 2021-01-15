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

/**
 * For using the JavaScript Code type.
 *
 * @since 3.0
 */
public class DtBsonJavaScript implements DtBsonValue {

    private final String code;

    /**
     * Construct a new instance with the given JavaScript code.
     *
     * @param code the Javascript code
     */
    public DtBsonJavaScript(final String code) {
        this.code = code;
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.JAVASCRIPT;
    }

    /**
     * Get the Javascript code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonJavaScript code1 = (DtBsonJavaScript) o;

        if (!code.equals(code1.code)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "DtBsonJavaScript{"
                + "code='" + code + '\''
                + '}';
    }
}

