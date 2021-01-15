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

import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.BsonValue;

/**
 * A representation of the JavaScript Code with Scope BSON type.
 *
 * @since 3.0
 */
public class DtBsonJavaScriptWithScope implements DtBsonValue {

    private final String code;
    private final DtBsonDocument scope;

    /**
     * Construct a new instance with the given code and scope.
     *
     * @param code the code
     * @param scope the scope
     */
    public DtBsonJavaScriptWithScope(final String code, final DtBsonDocument scope) {
        if (code == null) {
            throw new IllegalArgumentException("code can not be null");
        }
        if (scope == null) {
            throw new IllegalArgumentException("scope can not be null");
        }
        this.code = code;
        this.scope = scope;
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.JAVASCRIPT_WITH_SCOPE;
    }

    /**
     * Get the code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the scope.
     *
     * @return the scope
     */
    public DtBsonDocument getScope() {
        return scope;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonJavaScriptWithScope that = (DtBsonJavaScriptWithScope) o;

        if (!code.equals(that.code)) {
            return false;
        }
        if (!scope.equals(that.scope)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + scope.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DtBsonJavaScriptWithScope{"
               + "code=" + getCode()
               + "scope=" + scope
               + '}';
    }

    static DtBsonJavaScriptWithScope clone(final DtBsonJavaScriptWithScope from) {
        return new DtBsonJavaScriptWithScope(from.code, from.scope.clone());
    }
}

