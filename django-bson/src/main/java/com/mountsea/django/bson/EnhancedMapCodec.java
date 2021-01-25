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
package com.mountsea.django.bson;

import com.mountsea.django.bson.projection.pojo.ExternalMapStringKeyConverter;
import com.mountsea.django.bson.projection.pojo.GlobalModels;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.bson.assertions.Assertions.notNull;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

/**
 * A Codec for Map instances.
 *
 * @since 3.5
 */
public class EnhancedMapCodec implements Codec<Map<Object, Object>> {

    private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(asList(new ValueCodecProvider(), new BsonValueCodecProvider(),
            new EnhancedDocumentCodecProvider(), new EnhancedIterableCodecProvider(), new EnhancedMapCodecProvider()));
    private static final BsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP = new BsonTypeClassMap();
    private final BsonTypeCodecMap bsonTypeCodecMap;
    private final CodecRegistry registry;
    private final Transformer valueTransformer;
    private final Supplier<Map<Object, Object>> mapSupplier;

    /**
     * Construct a new instance with a default {@code CodecRegistry}
     */
    public EnhancedMapCodec() {
        this(DEFAULT_REGISTRY);
    }

    /**
     Construct a new instance with the given registry
     *
     * @param registry the registry
     */
    public EnhancedMapCodec(final CodecRegistry registry) {
        this(registry, DEFAULT_BSON_TYPE_CLASS_MAP, null);
    }

    /**
     * Construct a new instance with the given registry and BSON type class map.
     *
     * @param registry         the registry
     * @param bsonTypeClassMap the BSON type class map
     */
    public EnhancedMapCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap,
                            @Nullable final Supplier<Map<Object, Object>> mapSupplier) {
        this(registry, bsonTypeClassMap, null, mapSupplier);
    }

    /**
     * Construct a new instance with the given registry and BSON type class map. The transformer is applied as a last step when decoding
     * values, which allows users of this codec to control the decoding process.  For example, a user of this class could substitute a
     * value decoded as a Document with an instance of a special purpose class (e.g., one representing a DBRef in MongoDB).
     *
     * @param registry         the registry
     * @param bsonTypeClassMap the BSON type class map
     * @param valueTransformer the value transformer to use as a final step when decoding the value of any field in the map
     */
    public EnhancedMapCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap,
                            final Transformer valueTransformer, @Nullable final Supplier<Map<Object, Object>> mapSupplier) {
        this.registry = notNull("registry", registry);
        this.bsonTypeCodecMap = new BsonTypeCodecMap(notNull("bsonTypeClassMap", bsonTypeClassMap), registry);
        this.valueTransformer = valueTransformer != null ? valueTransformer : new Transformer() {
            @Override
            public Object transform(final Object value) {
                return value;
            }
        };
        this.mapSupplier = mapSupplier != null ? mapSupplier : HashMap::new;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void encode(final BsonWriter writer, final Map<Object, Object> map, final EncoderContext encoderContext) {
        Codec valueCodec = null;
        Class valueClass = null;
        ExternalMapStringKeyConverter keyConverter = null;
        Class keyClass = null;
        writer.writeStartDocument();
        for (final Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            if (keyConverter == null || keyClass != key.getClass()) {
                keyClass = key.getClass();
                keyConverter = GlobalModels.getOrCreateStringKeyConverter(keyClass);
            }
            writer.writeName(keyConverter.toString(key));
            Object value = entry.getValue();
            if (value == null) {
                writer.writeNull();
            } else {
                if (valueCodec == null || valueClass != value.getClass()) {
                    valueClass = value.getClass();
                    valueCodec = registry.get(value.getClass());
                }
                encoderContext.encodeWithChildContext(valueCodec, writer, value);
            }
        }
        writer.writeEndDocument();
    }

    @Override
    public Map<Object, Object> decode(final BsonReader reader, final DecoderContext decoderContext) {
        Map<Object, Object> map = mapSupplier.get();

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            map.put(fieldName, readValue(reader, decoderContext));
        }

        reader.readEndDocument();
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class<Map<Object, Object>> getEncoderClass() {
        return (Class<Map<Object, Object>>) ((Class) Map.class);
    }

    private Object readValue(final BsonReader reader, final DecoderContext decoderContext) {
        BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (bsonType == BsonType.ARRAY) {
            return decoderContext.decodeWithChildContext(registry.get(List.class), reader);
        } else if (bsonType == BsonType.BINARY && BsonBinarySubType.isUuid(reader.peekBinarySubType()) && reader.peekBinarySize() == 16) {
            return decoderContext.decodeWithChildContext(registry.get(UUID.class), reader);
        }
        return valueTransformer.transform(bsonTypeCodecMap.get(bsonType).decode(reader, decoderContext));
    }
}
