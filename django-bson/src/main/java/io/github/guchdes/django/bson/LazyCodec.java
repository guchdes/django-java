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

import io.github.guchdes.django.bson.util.LazyInitializer;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.function.Supplier;

/**
 * 解决死循环问题
 *
 * @Author guch
 * @Since 3.0.0
 */
public class LazyCodec<T> implements Codec<T> {
    private final LazyInitializer<Codec<T>> supplier;

    public LazyCodec(Supplier<Codec<T>> codecSupplier) {
        this.supplier = new LazyInitializer<>(codecSupplier);
    }

    private Codec<T> getCodec() {
        return supplier.get();
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        return getCodec().decode(reader, decoderContext);
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        getCodec().encode(writer, value, encoderContext);
    }

    @Override
    public Class<T> getEncoderClass() {
        return getCodec().getEncoderClass();
    }
}