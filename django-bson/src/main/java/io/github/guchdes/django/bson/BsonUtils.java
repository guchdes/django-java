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

import io.github.guchdes.django.bson.projection.dtbson.DtBsonDocument;
import io.github.guchdes.django.bson.projection.dtbson.DtBsonDocumentReader;
import io.github.guchdes.django.bson.projection.dtbson.DtBsonDocumentWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

import java.io.Reader;

/**
 * @author guch
 * @since 3.0.0
 */
public class BsonUtils {

    private static final CodecRegistry COMMON_CODEC_REGISTRY = new CommonCodecRegistry();

    public static CodecRegistry getCommonCodecRegistry() {
        return COMMON_CODEC_REGISTRY;
    }

    /* BsonDocument */

    public static BsonDocument toBsonDocument(Object o) {
        if (o == null) return null;
        return toBsonDocument(o, getCommonCodecRegistry());
    }

    @SuppressWarnings("unchecked")
    public static <T> BsonDocument toBsonDocument(T o, CodecRegistry codecRegistry) {
        if (o == null) return null;
        Codec<Object> codec = codecRegistry.get((Class<Object>) o.getClass());
        BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
        codec.encode(writer, o, EncoderContext.builder().build());
        return writer.getDocument();
    }

    public static <T> T fromBsonDocument(BsonDocument bsonDocument, Class<T> tClass, CodecRegistry codecRegistry) {
        Codec<T> codec = codecRegistry.get(tClass);
        BsonDocumentReader reader = new BsonDocumentReader(bsonDocument);
        return codec.decode(reader, DecoderContext.builder().build());
    }

    public static <T> T fromBsonDocument(BsonDocument bsonDocument, Class<T> tClass) {
        Codec<T> codec = getCommonCodecRegistry().get(tClass);
        BsonDocumentReader reader = new BsonDocumentReader(bsonDocument);
        return codec.decode(reader, DecoderContext.builder().build());
    }

    public static <T> T fromBson(Bson bson, Class<T> tClass) {
        if (bson instanceof BsonDocument) {
            return fromBsonDocument((BsonDocument) bson, tClass);
        } else {
            BsonDocument bsonDocument = toBsonDocument(bson);
            return fromBsonDocument(bsonDocument, tClass);
        }
    }

    /* json */

    public static String toJson(Object o) {
        if (o == null) return null;
        return toJson(o, getCommonCodecRegistry(), JsonWriterSettings.builder().build());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String toJson(Object o, CodecRegistry codecRegistry, JsonWriterSettings jsonWriterSettings) {
        if (o == null) return null;
        Codec codec = codecRegistry.get(o.getClass());

        StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
        JsonWriter writer = new JsonWriter(stringBuilderWriter, jsonWriterSettings);
        codec.encode(writer, o, EncoderContext.builder().build());
        return stringBuilderWriter.toString();
    }

    public static <T> T fromJson(String json, Class<T> aClass) {
        return fromJson(json, aClass, getCommonCodecRegistry());
    }

    public static <T> T fromJson(Reader json, Class<T> aClass) {
        return fromJson(json, aClass, getCommonCodecRegistry());
    }

    public static <T> T fromJson(Reader json, Class<T> aClass, CodecRegistry codecRegistry) {
        if (json == null) return null;
        Codec<T> codec = codecRegistry.get(aClass);
        JsonReader jsonReader = new JsonReader(json);
        return codec.decode(jsonReader, DecoderContext.builder().build());
    }

    public static <T> T fromJson(String json, Class<T> aClass, CodecRegistry codecRegistry) {
        if (json == null) return null;
        Codec<T> codec = codecRegistry.get(aClass);
        JsonReader jsonReader = new JsonReader(json);
        return codec.decode(jsonReader, DecoderContext.builder().build());
    }

    /* DtBsonDocument */

    public static DtBsonDocument toDtBsonDocument(Object o) {
        if (o == null) return null;
        return toDtBsonDocument(o, getCommonCodecRegistry());
    }

    @SuppressWarnings("unchecked")
    public static <T> DtBsonDocument toDtBsonDocument(T o, CodecRegistry codecRegistry) {
        if (o == null) return null;
        Codec<Object> codec = codecRegistry.get((Class<Object>) o.getClass());
        DtBsonDocumentWriter writer = new DtBsonDocumentWriter(new DtBsonDocument());
        codec.encode(writer, o, EncoderContext.builder().build());
        return writer.getDocument();
    }

    public static <T> T fromDtBsonDocument(DtBsonDocument bsonDocument, Class<T> tClass, CodecRegistry codecRegistry) {
        Codec<T> codec = codecRegistry.get(tClass);
        DtBsonDocumentReader reader = new DtBsonDocumentReader(bsonDocument);
        return codec.decode(reader, DecoderContext.builder().build());
    }

    public static <T> T fromDtBsonDocument(DtBsonDocument bsonDocument, Class<T> tClass) {
        Codec<T> codec = getCommonCodecRegistry().get(tClass);
        DtBsonDocumentReader reader = new DtBsonDocumentReader(bsonDocument);
        return codec.decode(reader, DecoderContext.builder().build());
    }

}
