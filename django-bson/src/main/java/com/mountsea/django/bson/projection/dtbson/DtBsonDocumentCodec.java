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
package com.mountsea.django.bson.projection.dtbson;

import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mountsea.django.bson.projection.dtbson.DtBsonValueCodecProvider.getDtBsonTypeClassMap;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

/**
 * A codec for DtBsonDocument instances.
 *
 * @since 3.0
 */
public class DtBsonDocumentCodec implements Codec<DtBsonDocument> {
    private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(new DtBsonValueCodecProvider());

    private final CodecRegistry codecRegistry;
    private final DtBsonTypeCodecMap dtBsonTypeCodecMap;

    /**
     * Creates a new instance with a default codec registry that uses the {@link DtBsonValueCodecProvider}.
     */
    public DtBsonDocumentCodec() {
        this(DEFAULT_REGISTRY);
    }

    /**
     * Creates a new instance initialised with the given codec registry.
     *
     * @param codecRegistry the {@code CodecRegistry} to use to look up the codecs for encoding and decoding to/from BSON
     */
    public DtBsonDocumentCodec(final CodecRegistry codecRegistry) {
        if (codecRegistry == null) {
            throw new IllegalArgumentException("Codec registry can not be null");
        }
        this.codecRegistry = codecRegistry;
        this.dtBsonTypeCodecMap = new DtBsonTypeCodecMap(getDtBsonTypeClassMap(), codecRegistry);
    }

    /**
     * Gets the {@code CodecRegistry} for this {@code Codec}.
     *
     * @return the registry
     */
    public CodecRegistry getCodecRegistry() {
        return codecRegistry;
    }

    @Override
    public DtBsonDocument decode(final BsonReader reader, final DecoderContext decoderContext) {
        List<DtBsonElement> keyValuePairs = new ArrayList<DtBsonElement>();

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            keyValuePairs.add(new DtBsonElement(fieldName, readValue(reader, decoderContext)));
        }

        reader.readEndDocument();

        return new DtBsonDocument(keyValuePairs);
    }

    /**
     * This method may be overridden to change the behavior of reading the current value from the given {@code BsonReader}.  It is required
     * that the value be fully consumed before returning.
     *
     * @param reader         the read to read the value from
     * @param decoderContext the context
     * @return the non-null value read from the reader
     */
    protected DtBsonValue readValue(final BsonReader reader, final DecoderContext decoderContext) {
        return (DtBsonValue) dtBsonTypeCodecMap.get(reader.getCurrentBsonType()).decode(reader, decoderContext);
    }

    @Override
    public void encode(final BsonWriter writer, final DtBsonDocument value, final EncoderContext encoderContext) {
        writer.writeStartDocument();

        for (Map.Entry<String, DtBsonValue> entry : value.entrySet()) {
            writer.writeName(entry.getKey());
            writeValue(writer, encoderContext, entry.getValue());
        }

        writer.writeEndDocument();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeValue(final BsonWriter writer, final EncoderContext encoderContext, final DtBsonValue value) {
        Codec codec = codecRegistry.get(value.getClass());
        encoderContext.encodeWithChildContext(codec, writer, value);
    }

    @Override
    public Class<DtBsonDocument> getEncoderClass() {
        return DtBsonDocument.class;
    }

}
