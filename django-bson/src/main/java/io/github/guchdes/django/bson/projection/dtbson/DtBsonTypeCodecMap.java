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
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistry;

import static java.lang.String.format;
import static org.bson.assertions.Assertions.notNull;

/**
 * An efficient map of BsonType to Codec
 *
 * @since 3.3
 */
public class DtBsonTypeCodecMap {
    private final DtBsonTypeClassMap bsonTypeClassMap;
    private final Codec<?>[] codecs = new Codec<?>[256];

    /**
     * Initializes the map by iterating the keys of the given BsonTypeClassMap and looking up the Codec for the Class mapped to each key.
     * @param bsonTypeClassMap the non-null BsonTypeClassMap
     * @param codecRegistry the non-null CodecRegistry
     */
    public DtBsonTypeCodecMap(final DtBsonTypeClassMap bsonTypeClassMap, final CodecRegistry codecRegistry) {
        this.bsonTypeClassMap = notNull("bsonTypeClassMap", bsonTypeClassMap);
        notNull("codecRegistry", codecRegistry);
        for (BsonType cur : bsonTypeClassMap.keys()) {
            Class<?> clazz = bsonTypeClassMap.get(cur);
            if (clazz != null) {
                try {
                    codecs[cur.getValue()] = codecRegistry.get(clazz);
                } catch (CodecConfigurationException e) {
                    // delay reporting this until the codec is actually requested
                }
            }
        }
    }

    /**
     * Gets the Codec mapped to the given bson type.
     *
     * @param bsonType the non-null BsonType
     * @return the non-null Codec
     */
    public Codec<?> get(final BsonType bsonType) {
        Codec<?> codec = codecs[bsonType.getValue()];
        if (codec == null) {
            Class<?> clazz = bsonTypeClassMap.get(bsonType);
            if (clazz == null) {
                throw new CodecConfigurationException(format("No class mapped for BSON type %s.", bsonType));
            } else {
                throw new CodecConfigurationException(format("Can't find a codec for %s.", clazz));
            }
        }
        return codec;
    }
}
