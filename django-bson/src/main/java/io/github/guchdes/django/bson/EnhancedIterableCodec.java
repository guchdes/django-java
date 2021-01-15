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
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bson.assertions.Assertions.notNull;

/**
 * Encodes and decodes {@code Iterable} objects.
 *
 * @since 3.3
 */
@SuppressWarnings("rawtypes")
public class EnhancedIterableCodec implements Codec<Iterable> {

    private final CodecRegistry registry;
    private final BsonTypeCodecMap bsonTypeCodecMap;
    private final Transformer valueTransformer;

    /**
     * Construct a new instance with the given {@code CodecRegistry} and {@code BsonTypeClassMap}.
     *
     * @param registry the non-null codec registry
     * @param bsonTypeClassMap the non-null BsonTypeClassMap
     */
    public EnhancedIterableCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap) {
        this(registry, bsonTypeClassMap, null);
    }

    /**
     * Construct a new instance with the given {@code CodecRegistry} and {@code BsonTypeClassMap}.
     *
     * @param registry the non-null codec registry
     * @param bsonTypeClassMap the non-null BsonTypeClassMap
     * @param valueTransformer the value Transformer
     */
    public EnhancedIterableCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap, final Transformer valueTransformer) {
        this.registry = notNull("registry", registry);
        this.bsonTypeCodecMap = new BsonTypeCodecMap(notNull("bsonTypeClassMap", bsonTypeClassMap), registry);
        this.valueTransformer = valueTransformer != null ? valueTransformer : new Transformer() {
            @Override
            public Object transform(final Object objectToTransform) {
                return objectToTransform;
            }
        };
    }

    @Override
    public Iterable decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();

        List<Object> list = new ArrayList<Object>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(readValue(reader, decoderContext));
        }

        reader.readEndArray();

        return list;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void encode(final BsonWriter writer, final Iterable value, final EncoderContext encoderContext) {
        writer.writeStartArray();
        Codec elementCodec = null;
        Class elementClass = null;
        for (final Object cur : value) {
            if (cur == null) {
                writer.writeNull();
            } else {
                if (elementCodec == null || elementClass != cur.getClass()) {
                    elementClass = cur.getClass();
                    elementCodec = registry.get(elementClass);
                }
                encoderContext.encodeWithChildContext(elementCodec, writer, cur);
            }
        }
        writer.writeEndArray();
    }

    @Override
    public Class<Iterable> getEncoderClass() {
        return Iterable.class;
    }

    private Object readValue(final BsonReader reader, final DecoderContext decoderContext) {
        BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (bsonType == BsonType.BINARY && BsonBinarySubType.isUuid(reader.peekBinarySubType()) && reader.peekBinarySize() == 16) {
            return registry.get(UUID.class).decode(reader, decoderContext);
        }
        return valueTransformer.transform(bsonTypeCodecMap.get(bsonType).decode(reader, decoderContext));
    }
}
