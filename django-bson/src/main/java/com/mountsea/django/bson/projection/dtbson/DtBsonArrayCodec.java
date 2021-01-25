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

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.bson.assertions.Assertions.notNull;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

/**
 * A codec for DtBsonArray instances.
 *
 * @since 3.0
 */
public class DtBsonArrayCodec implements Codec<DtBsonArray> {

    private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(new BsonValueCodecProvider());

    private final CodecRegistry codecRegistry;

    /**
     * Creates a new instance with a default codec registry that uses the {@link BsonValueCodecProvider}.
     *
     * @since 3.4
     */
    public DtBsonArrayCodec() {
        this(DEFAULT_REGISTRY);
    }

    /**
     * Construct an instance with the given registry
     *
     * @param codecRegistry the codec registry
     */
    public DtBsonArrayCodec(final CodecRegistry codecRegistry) {
        this.codecRegistry = notNull("codecRegistry", codecRegistry);
    }

    @Override
    public DtBsonArray decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();

        List<DtBsonValue> list = new ArrayList<DtBsonValue>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(readValue(reader, decoderContext));
        }

        reader.readEndArray();

        return new DtBsonArray(list);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void encode(final BsonWriter writer, final DtBsonArray array, final EncoderContext encoderContext) {
        writer.writeStartArray();

        for (DtBsonValue value : array) {
            Codec codec = codecRegistry.get(value.getClass());
            encoderContext.encodeWithChildContext(codec, writer, value);
        }

        writer.writeEndArray();
    }

    @Override
    public Class<DtBsonArray> getEncoderClass() {
        return DtBsonArray.class;
    }

    /**
     * This method may be overridden to change the behavior of reading the current value from the given {@code BsonReader}.  It is required
     * that the value be fully consumed before returning.
     *
     * @param reader the read to read the value from
     * @param decoderContext the decoder context
     * @return the non-null value read from the reader
     */
    protected DtBsonValue readValue(final BsonReader reader, final DecoderContext decoderContext) {
        Class<? extends DtBsonValue> type = DtBsonValueCodecProvider.getClassForBsonType(reader.getCurrentBsonType());
        return codecRegistry.get(type).decode(reader, decoderContext);
    }

}
