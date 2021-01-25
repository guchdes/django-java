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

import com.mountsea.django.bson.projection.IndexedEnum;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.function.Function;

/**
 * @author guch
 * @since 3.0.0
 */
public class EnumCodecProvider implements CodecProvider {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (Enum.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new EnumCodec(clazz);
        }
        return null;
    }

    private static class EnumCodec<T extends Enum<T>> implements Codec<T> {
        private final Class<T> clazz;
        private final boolean isIndexedEnum;
        private final Function<Integer, T> indexConverter;

        @SuppressWarnings({"unchecked", "rawtypes"})
        EnumCodec(final Class<T> clazz) {
            this.clazz = clazz;
            if (IndexedEnum.class.isAssignableFrom(clazz)) {
                isIndexedEnum = true;
                indexConverter = (Function<Integer, T>) IndexedEnumHelper.getIndexToEnumConverter((Class) clazz);
            } else {
                isIndexedEnum = false;
                indexConverter = null;
            }
        }

        @Override
        public void encode(final BsonWriter writer, final T value, final EncoderContext encoderContext) {
            if (isIndexedEnum) {
                writer.writeInt32(((IndexedEnum) value).getIndex());
            } else {
                writer.writeString(value.name());
            }
        }

        @Override
        public Class<T> getEncoderClass() {
            return clazz;
        }

        @Override
        public T decode(final BsonReader reader, final DecoderContext decoderContext) {
            if (isIndexedEnum) {
                return indexConverter.apply(reader.readInt32());
            } else {
                return Enum.valueOf(clazz, reader.readString());
            }
        }
    }

}
