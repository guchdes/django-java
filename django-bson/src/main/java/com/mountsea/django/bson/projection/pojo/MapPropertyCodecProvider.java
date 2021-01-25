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
package com.mountsea.django.bson.projection.pojo;

import com.mountsea.django.bson.projection.DocumentEnumMap;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;

import java.util.*;
import java.util.Map.Entry;

import static java.lang.String.format;

final class MapPropertyCodecProvider implements PropertyCodecProvider {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <T> Codec<T> get(final TypeWithTypeParameters<T> type, final PropertyCodecRegistry registry) {
        if (Map.class.isAssignableFrom(type.getType()) && type.getTypeParameters().size() == 2) {
            Class<?> keyType = type.getTypeParameters().get(0).getType();
            boolean isEnumMap = isEnumMap(type.getType());
            ExternalMapStringKeyConverter<?> converter = GlobalModels.getOrCreateStringKeyConverter(keyType);
            try {
                return new MapCodec(type.getType(), registry.get(type.getTypeParameters().get(1)), converter,
                        isEnumMap ? keyType : null);
            } catch (CodecConfigurationException e) {
                if (type.getTypeParameters().get(1).getType() == Object.class) {
                    try {
                        return (Codec<T>) registry.get(TypeData.builder(Map.class).build());
                    } catch (CodecConfigurationException e1) {
                        // Ignore and return original exception
                    }
                }
                throw e;
            }
        } else {
            return null;
        }
    }

    static boolean isEnumMap(Class<?> aClass) {
        if (EnumMap.class.isAssignableFrom(aClass) || DocumentEnumMap.class.isAssignableFrom(aClass)) {
            return true;
        } else {
            return false;
        }
    }

    private static class MapCodec<K, T, E extends Enum<E>> implements Codec<Map<K, T>> {
        private final Class<Map<K, T>> encoderClass;
        private final Codec<T> codec;
        private final ExternalMapStringKeyConverter<K> converter;

        //如果enumClass不为null，表示当前是EnumMap
        private final Class<E> enumClass;

        MapCodec(Class<Map<K, T>> encoderClass, Codec<T> codec, ExternalMapStringKeyConverter<K> converter,
                 Class<E> enumClass) {
            this.encoderClass = encoderClass;
            this.codec = codec;
            this.converter = converter;
            this.enumClass = enumClass;
        }

        @Override
        public void encode(final BsonWriter writer, final Map<K, T> map, final EncoderContext encoderContext) {
            writer.writeStartDocument();
            for (final Entry<K, T> entry : map.entrySet()) {
                writer.writeName(converter.toString(entry.getKey()));
                if (entry.getValue() == null) {
                    writer.writeNull();
                } else {
                    codec.encode(writer, entry.getValue(), encoderContext);
                }
            }
            writer.writeEndDocument();
        }

        @Override
        public Map<K, T> decode(final BsonReader reader, final DecoderContext context) {
            reader.readStartDocument();
            Map<K, T> map = getInstance();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                if (reader.getCurrentBsonType() == BsonType.NULL) {
                    map.put(converter.fromString(reader.readName()), null);
                    reader.readNull();
                } else {
                    map.put(converter.fromString(reader.readName()), codec.decode(reader, context));
                }
            }
            reader.readEndDocument();
            return map;
        }

        @Override
        public Class<Map<K, T>> getEncoderClass() {
            return encoderClass;
        }

        private Map<K, T> getInstance() {
            if (encoderClass.isInterface()) {
                if (SortedMap.class.isAssignableFrom(encoderClass)) {
                    return new TreeMap<>();
                } else {
                    return new HashMap<>();
                }
            }
            try {
                if (enumClass != null) {
                    return encoderClass.getDeclaredConstructor(Class.class).newInstance(enumClass);
                } else {
                    return encoderClass.getDeclaredConstructor().newInstance();
                }
            } catch (final Exception e) {
                throw new CodecConfigurationException(e.getMessage(), e);
            }
        }
    }
}
