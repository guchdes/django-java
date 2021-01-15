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
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@code CodecProvider} for all subclass of BsonValue.
 *
 * @since 3.0
 */
public class DtBsonValueCodecProvider implements CodecProvider {
    private static final DtBsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP;

    private final Map<Class<?>, Codec<?>> codecs = new HashMap<Class<?>, Codec<?>>();

    /**
     * Construct a new instance with the default codec for each BSON type.
     */
    public DtBsonValueCodecProvider() {
        addCodecs();
    }

    /**
     * Get the {@code BsonValue} subclass associated with the given {@code BsonType}.
     * @param bsonType the BsonType
     * @return the class associated with the given type
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends DtBsonValue> getClassForBsonType(final BsonType bsonType) {
        return (Class<? extends DtBsonValue>) DEFAULT_BSON_TYPE_CLASS_MAP.get(bsonType);
    }

    /**
     * Gets the BsonTypeClassMap used by this provider.
     *
     * @return the non-null BsonTypeClassMap
     * @since 3.3
     */
    public static DtBsonTypeClassMap getDtBsonTypeClassMap() {
        return DEFAULT_BSON_TYPE_CLASS_MAP;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (codecs.containsKey(clazz)) {
            return (Codec<T>) codecs.get(clazz);
        }

        if (clazz == DtBsonJavaScriptWithScope.class) {
            return (Codec<T>) new DtBsonJavaScriptWithScopeCodec(registry.get(DtBsonDocument.class));
        }

        if (clazz == DtBsonValue.class) {
            return (Codec<T>) new DtBsonValueCodec(registry);
        }

        if (DtBsonDocument.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new DtBsonDocumentCodec(registry);
        }

        if (DtBsonArray.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new DtBsonArrayCodec(registry);
        }

        return null;
    }

    private void addCodecs() {
        addCodec(new DtBsonNullCodec());
        addCodec(new DtBsonBinaryCodec());
        addCodec(new DtBsonBooleanCodec());
        addCodec(new DtBsonDateTimeCodec());
        addCodec(new DtBsonDBPointerCodec());
        addCodec(new DtBsonDoubleCodec());
        addCodec(new DtBsonInt32Codec());
        addCodec(new DtBsonInt64Codec());
        addCodec(new DtBsonDecimal128Codec());
        addCodec(new DtBsonMinKeyCodec());
        addCodec(new DtBsonMaxKeyCodec());
        addCodec(new DtBsonJavaScriptCodec());
        addCodec(new DtBsonObjectIdCodec());
        addCodec(new DtBsonRegularExpressionCodec());
        addCodec(new DtBsonStringCodec());
        addCodec(new DtBsonSymbolCodec());
        addCodec(new DtBsonTimestampCodec());
        addCodec(new DtBsonUndefinedCodec());
    }

    private <T extends DtBsonValue> void addCodec(final Codec<T> codec) {
        codecs.put(codec.getEncoderClass(), codec);
    }

    static {
        Map<BsonType, Class<?>> map = new HashMap<BsonType, Class<?>>();

        map.put(BsonType.NULL, DtBsonNull.class);
        map.put(BsonType.ARRAY, DtBsonArray.class);
        map.put(BsonType.BINARY, DtBsonBinary.class);
        map.put(BsonType.BOOLEAN, DtBsonBoolean.class);
        map.put(BsonType.DATE_TIME, DtBsonDateTime.class);
        map.put(BsonType.DB_POINTER, DtBsonDbPointer.class);
        map.put(BsonType.DOCUMENT, DtBsonDocument.class);
        map.put(BsonType.DOUBLE, DtBsonDouble.class);
        map.put(BsonType.INT32, DtBsonInt32.class);
        map.put(BsonType.INT64, DtBsonInt64.class);
        map.put(BsonType.DECIMAL128, DtBsonDecimal128.class);
        map.put(BsonType.MAX_KEY, DtBsonMaxKey.class);
        map.put(BsonType.MIN_KEY, DtBsonMinKey.class);
        map.put(BsonType.JAVASCRIPT, DtBsonJavaScript.class);
        map.put(BsonType.JAVASCRIPT_WITH_SCOPE, DtBsonJavaScriptWithScope.class);
        map.put(BsonType.OBJECT_ID, DtBsonObjectId.class);
        map.put(BsonType.REGULAR_EXPRESSION, DtBsonRegularExpression.class);
        map.put(BsonType.STRING, DtBsonString.class);
        map.put(BsonType.SYMBOL, DtBsonSymbol.class);
        map.put(BsonType.TIMESTAMP, DtBsonTimestamp.class);
        map.put(BsonType.UNDEFINED, DtBsonUndefined.class);

        DEFAULT_BSON_TYPE_CLASS_MAP = new DtBsonTypeClassMap(map);
    }
}
