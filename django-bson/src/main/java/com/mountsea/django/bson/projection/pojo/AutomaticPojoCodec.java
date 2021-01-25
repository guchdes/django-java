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
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import static java.lang.String.format;

final class AutomaticPojoCodec<T> extends PojoCodec<T> {
    private final PojoCodec<T> pojoCodec;

    AutomaticPojoCodec(final PojoCodec<T> pojoCodec) {
        this.pojoCodec = pojoCodec;
    }

    @Override
    public T decode(final BsonReader reader, final DecoderContext decoderContext) {
        try {
            return pojoCodec.decode(reader, decoderContext);
        } catch (CodecConfigurationException e) {
            throw new CodecConfigurationException(
                    format("An exception occurred when decoding using the AutomaticPojoCodec.%n"
                            + "Decoding into a '%s' failed with the following exception:%n%n%s%n%n"
                            + "A custom Codec or PojoCodec may need to be explicitly configured and registered to handle this type.",
                            pojoCodec.getEncoderClass().getSimpleName(), e.getMessage()), e);
        }
    }

    @Override
    public void encode(final BsonWriter writer, final T value, final EncoderContext encoderContext) {
        try {
            pojoCodec.encode(writer, value, encoderContext);
        } catch (CodecConfigurationException e) {
            throw new CodecConfigurationException(
                    format("An exception occurred when encoding using the AutomaticPojoCodec.%n"
                            + "Encoding a %s: '%s' failed with the following exception:%n%n%s%n%n"
                            + "A custom Codec or PojoCodec may need to be explicitly configured and registered to handle this type.",
                            getEncoderClass().getSimpleName(), value, e.getMessage()), e);
        }
    }

    @Override
    public Class<T> getEncoderClass() {
        return pojoCodec.getEncoderClass();
    }

    @Override
    ClassModel<T> getClassModel() {
        return pojoCodec.getClassModel();
    }
}
