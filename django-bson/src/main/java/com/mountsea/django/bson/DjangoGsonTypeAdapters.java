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


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mountsea.django.bson.projection.ContainerDocumentNode;
import com.mountsea.django.bson.projection.DocumentNode;
import com.mountsea.django.bson.projection.NotProxy;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonWriterSettings;

import java.io.IOException;

public class DjangoGsonTypeAdapters {

    public static TypeAdapterFactory getDocumentNodeAdapterFactory() {
        return getDocumentNodeAdapterFactory(BsonUtils.getCommonCodecRegistry());
    }

    public static TypeAdapterFactory getDocumentNodeAdapterFactory(CodecRegistry codecRegistry) {
        return new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (DocumentNode.class.isAssignableFrom(type.getRawType()) &&
                        !ContainerDocumentNode.class.isAssignableFrom(type.getRawType()) &&
                        !NotProxy.class.isAssignableFrom(type.getRawType())) {
                    return new TypeAdapter<T>() {
                        @Override
                        public void write(JsonWriter out, T value) throws IOException {
                            String json = BsonUtils.toJson(value, codecRegistry, JsonWriterSettings.builder().build());
                            out.jsonValue(json);
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public T read(JsonReader in) throws IOException {
                            JsonElement jsonElement = TypeAdapters.JSON_ELEMENT.read(in);
                            T document = (T) BsonUtils.fromJson(jsonElement.toString(), type.getRawType(), codecRegistry);
                            return document;
                        }
                    };
                }
                return null;
            }
        };
    }


}
