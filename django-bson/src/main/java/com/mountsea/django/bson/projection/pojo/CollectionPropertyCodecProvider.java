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

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;

import java.util.*;

import static java.lang.String.format;

final class CollectionPropertyCodecProvider implements PropertyCodecProvider {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <T> Codec<T> get(final TypeWithTypeParameters<T> type, final PropertyCodecRegistry registry) {
        if (Collection.class.isAssignableFrom(type.getType()) && type.getTypeParameters().size() == 1) {
            return new CollectionCodec(type.getType(), registry.get(type.getTypeParameters().get(0)));
        } else {
            return null;
        }
    }

    private static class CollectionCodec<T> implements Codec<Collection<T>> {
        private final Class<Collection<T>> encoderClass;
        private final Codec<T> codec;

        CollectionCodec(final Class<Collection<T>> encoderClass, final Codec<T> codec) {
            this.encoderClass = encoderClass;
            this.codec = codec;
        }

        @Override
        public void encode(final BsonWriter writer, final Collection<T> collection, final EncoderContext encoderContext) {
            writer.writeStartArray();
            for (final T value : collection) {
                if (value == null) {
                    writer.writeNull();
                } else {
                    codec.encode(writer, value, encoderContext);
                }
            }
            writer.writeEndArray();
        }

        @Override
        public Collection<T> decode(final BsonReader reader, final DecoderContext context) {
            Collection<T> collection = getInstance();
            reader.readStartArray();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                if (reader.getCurrentBsonType() == BsonType.NULL) {
                    collection.add(null);
                    reader.readNull();
                } else {
                    collection.add(codec.decode(reader, context));
                }
            }
            reader.readEndArray();
            return collection;
        }

        @Override
        public Class<Collection<T>> getEncoderClass() {
            return encoderClass;
        }

        private Collection<T> getInstance() {
            if (encoderClass.isInterface()) {
                if (encoderClass.isAssignableFrom(ArrayList.class)) {
                    return new ArrayList<T>();
                } else if (encoderClass.isAssignableFrom(LinkedList.class)) {
                    return new LinkedList<>();
                } else if (encoderClass.isAssignableFrom(HashSet.class)) {
                    return new HashSet<T>();
                } else if (encoderClass.isAssignableFrom(TreeSet.class)) {
                    return new TreeSet<>();
                } else {
                    throw new CodecConfigurationException(format("Unsupported Collection interface of %s!", encoderClass.getName()));
                }
            }

            try {
                return encoderClass.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                throw new CodecConfigurationException(e.getMessage(), e);
            }
        }
    }
}
