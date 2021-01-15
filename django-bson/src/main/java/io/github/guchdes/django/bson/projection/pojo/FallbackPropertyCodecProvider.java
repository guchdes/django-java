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
package io.github.guchdes.django.bson.projection.pojo;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;
final class FallbackPropertyCodecProvider implements PropertyCodecProvider {
    private final CodecRegistry codecRegistry;
    private final PojoCodec<?> pojoCodec;

    FallbackPropertyCodecProvider(final PojoCodec<?> pojoCodec, final CodecRegistry codecRegistry) {
        this.pojoCodec = pojoCodec;
        this.codecRegistry = codecRegistry;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> Codec<S> get(final TypeWithTypeParameters<S> type, final PropertyCodecRegistry propertyCodecRegistry) {
        Class<S> clazz = type.getType();
        if (clazz == pojoCodec.getEncoderClass()) {
            return (Codec<S>) pojoCodec;
        }
        return codecRegistry.get(type.getType());
    }
}
