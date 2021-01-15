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

import org.bson.*;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.types.*;

import java.util.*;

/**
 * <p>A map from a BSON types to the Class to which it should be decoded.  This class is useful if, for example,
 * you want to change the default decoding of BSON DATE to something besides {@code java.util.Date}.</p>
 *
 * <p>The default mappings are:</p>
 *
 * <ul>
 *     <li><em>DOCUMENT</em>: {@code org.bson.Document.class} </li>
 *     <li><em>ARRAY</em>: {@code java.util.List.class} </li>
 *     <li><em>DATE_TIME</em>: {@code java.util.Date.class} </li>
 *     <li><em>BOOLEAN</em>: {@code java.lang.Boolean.class} </li>
 *     <li><em>DOUBLE</em>: {@code java.lang.Double.class} </li>
 *     <li><em>INT32</em>: {@code java.lang.Integer.class} </li>
 *     <li><em>INT64</em>: {@code java.lang.Long.class} </li>
 *     <li><em>DECIMAL128</em>: {@code org.bson.types.Decimal128.class} </li>
 *     <li><em>STRING</em>: {@code java.lang.String.class} </li>
 *     <li><em>BINARY</em>: {@code org.bson.types.Binary.class} </li>
 *     <li><em>OBJECT_ID</em>: {@code org.bson.types.ObjectId.class} </li>
 *     <li><em>REGULAR_EXPRESSION</em>: {@code org.bson.types.RegularExpression.class} </li>
 *     <li><em>SYMBOL</em>: {@code org.bson.types.Symbol.class} </li>
 *     <li><em>DB_POINTER</em>: {@code org.bson.types.DBPointer.class} </li>
 *     <li><em>MAX_KEY</em>: {@code org.bson.types.MaxKey.class} </li>
 *     <li><em>MIN_KEY</em>: {@code org.bson.types.MinKey.class} </li>
 *     <li><em>JAVASCRIPT</em>: {@code org.bson.types.Code.class} </li>
 *     <li><em>JAVASCRIPT_WITH_SCOPE</em>: {@code org.bson.types.CodeWithScope.class} </li>
 *     <li><em>TIMESTAMP</em>: {@code org.bson.types.BSONTimestamp.class} </li>
 *     <li><em>UNDEFINED</em>: {@code org.bson.types.Undefined.class} </li>
 * </ul>
 *
 * @since 3.0
 */
public class DtBsonTypeClassMap {
    private final Map<BsonType, Class<?>> map = new HashMap<BsonType, Class<?>>();

    /**
     * Construct an instance with the default mapping, but replacing the default mapping with any values contained in the given map.
     * This allows a caller to easily replace a single or a few mappings, while leaving the rest at their default values.
     *
     * @param replacementsForDefaults the replacement mappings
     */
    public DtBsonTypeClassMap(final Map<BsonType, Class<?>> replacementsForDefaults) {
        addDefaults();
        map.putAll(replacementsForDefaults);
    }

    /**
     * Construct an instance with the default mappings.
     */
    public DtBsonTypeClassMap() {
        this(Collections.<BsonType, Class<?>>emptyMap());
    }

    Set<BsonType> keys() {
        return map.keySet();
    }

    /**
     * Gets the Class that is mapped to the given BSON type.
     *
     * @param bsonType the BSON type
     * @return the Class that is mapped to the BSON type
     */
    public Class<?> get(final BsonType bsonType) {
        return map.get(bsonType);
    }

    private void addDefaults() {
        map.put(BsonType.ARRAY, List.class);
        map.put(BsonType.BINARY, Binary.class);
        map.put(BsonType.BOOLEAN, Boolean.class);
        map.put(BsonType.DATE_TIME, Date.class);
        map.put(BsonType.DB_POINTER, BsonDbPointer.class);
        map.put(BsonType.DOCUMENT, Document.class);
        map.put(BsonType.DOUBLE, Double.class);
        map.put(BsonType.INT32, Integer.class);
        map.put(BsonType.INT64, Long.class);
        map.put(BsonType.DECIMAL128, Decimal128.class);
        map.put(BsonType.MAX_KEY, MaxKey.class);
        map.put(BsonType.MIN_KEY, MinKey.class);
        map.put(BsonType.JAVASCRIPT, Code.class);
        map.put(BsonType.JAVASCRIPT_WITH_SCOPE, CodeWithScope.class);
        map.put(BsonType.OBJECT_ID, ObjectId.class);
        map.put(BsonType.REGULAR_EXPRESSION, BsonRegularExpression.class);
        map.put(BsonType.STRING, String.class);
        map.put(BsonType.SYMBOL, Symbol.class);
        map.put(BsonType.TIMESTAMP, BsonTimestamp.class);
        map.put(BsonType.UNDEFINED, BsonUndefined.class);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DtBsonTypeClassMap that = (DtBsonTypeClassMap) o;

        if (!map.equals(that.map)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
