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
package io.github.guchdes.django.bson;

import org.bson.*;
import org.bson.codecs.ValueCodecProvider;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Set;

/**
 * @author guch
 * @since 3.0.0
 */
public class KnownTypes {

    private static final ValueCodecProvider VALUE_CODEC_PROVIDER = new ValueCodecProvider();

    private static final Set<Class<?>> KNOWN_SIMPLE_IMMUTABLE_TYPES;

    static {
        /************* bson value type **************/
        KNOWN_SIMPLE_IMMUTABLE_TYPES = new HashSet<>();
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonNull.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonBoolean.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonDateTime.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonDbPointer.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonDouble.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonInt32.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonInt64.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonDecimal128.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonMinKey.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonMaxKey.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonJavaScript.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonObjectId.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonRegularExpression.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonString.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonSymbol.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonTimestamp.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(BsonUndefined.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(ObjectId.class);
        KNOWN_SIMPLE_IMMUTABLE_TYPES.add(Object.class);
    }

    public static boolean isSimpleAndImmutableType(Class<?> aClass) {
        return VALUE_CODEC_PROVIDER.get(aClass, null) != null ||
                KNOWN_SIMPLE_IMMUTABLE_TYPES.contains(aClass);
    }
}
