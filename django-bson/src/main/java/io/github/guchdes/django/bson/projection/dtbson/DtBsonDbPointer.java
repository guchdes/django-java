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
import org.bson.types.ObjectId;

/**
 * Holder for a BSON type DBPointer(0x0c). It's deprecated in BSON Specification and present here because of compatibility reasons.
 *
 * @since 3.0
 */
public class DtBsonDbPointer implements DtBsonValue {

    private final String namespace;
    private final ObjectId id;

    /**
     * Construct a new instance with the given namespace and id.
     *
     * @param namespace the namespace
     * @param id the id
     */
    public DtBsonDbPointer(final String namespace, final ObjectId id) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace can not be null");
        }
        if (id == null) {
            throw new IllegalArgumentException("id can not be null");
        }
        this.namespace = namespace;
        this.id = id;
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.DB_POINTER;
    }

    /**
     * Gets the namespace.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public ObjectId getId() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DtBsonDbPointer dbPointer = (DtBsonDbPointer) o;

        if (!id.equals(dbPointer.id)) {
            return false;
        }
        if (!namespace.equals(dbPointer.namespace)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DtBsonDbPointer{"
               + "namespace='" + namespace + '\''
               + ", id=" + id
               + '}';
    }
}
