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

import io.github.guchdes.django.bson.projection.LinkedDocumentMap;
import io.github.guchdes.django.bson.projection.DocumentList;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistry;

import static org.bson.codecs.BsonValueCodecProvider.getBsonTypeClassMap;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class ObjectCodec implements Codec<Object> {

    private final CodecRegistry codecRegistry;
    private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(new BsonValueCodecProvider());

    private final BsonTypeCodecMap bsonTypeCodecMap;

    public ObjectCodec() {
        this(DEFAULT_REGISTRY);
    }

    public ObjectCodec(final CodecRegistry codecRegistry) {
        if (codecRegistry == null) {
            throw new IllegalArgumentException("Codec registry can not be null");
        }
        this.codecRegistry = codecRegistry;
        this.bsonTypeCodecMap = new BsonTypeCodecMap(getBsonTypeClassMap(), codecRegistry);
    }

    @Override
    public Object decode(BsonReader reader, DecoderContext decoderContext) {
        return readValue(reader, decoderContext);
    }

    private Object readValue(final BsonReader reader, final DecoderContext decoderContext) {
        BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType == BsonType.DOCUMENT) {
            LinkedDocumentMap<String, Object> map = new LinkedDocumentMap<>();
            reader.readStartDocument();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                String fieldName = reader.readName();
                map.put(fieldName, readValue(reader, decoderContext));
            }
            reader.readEndDocument();
            return map;
        } else if (bsonType == BsonType.ARRAY) {
            DocumentList<Object> list = new DocumentList<>();
            reader.readStartArray();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                list.add(readValue(reader, decoderContext));
            }
            reader.readEndArray();
            return list;
        } else {
            return bsonTypeCodecMap.get(reader.getCurrentBsonType()).decode(reader, decoderContext);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void encode(BsonWriter writer, Object value, EncoderContext encoderContext) {
        if (value.getClass().equals(Object.class)) {
            //如果当前是在encode map的value，必须有一个值，可以是一个空文档
            writer.writeStartDocument();
            writer.writeEndDocument();
            return;
        }
        Codec<Object> codec = codecRegistry.get((Class<Object>) value.getClass());
        if (codec != null) {
            codec.encode(writer, value, encoderContext);
        } else {
            throw new CodecConfigurationException("Not found codec for:" + value.getClass());
        }
    }

    @Override
    public Class<Object> getEncoderClass() {
        return Object.class;
    }
}
