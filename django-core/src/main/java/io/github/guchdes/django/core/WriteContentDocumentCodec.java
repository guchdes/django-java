/**
 * MIT License
 *
 * Copyright (c) 2021 fengniao studio
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
package io.github.guchdes.django.core;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * @Author guch
 * @Since 3.0.0
 */
class WriteContentDocumentCodec implements Codec<WriteContentDocument> {

    private final CodecRegistry codecRegistry;

    public WriteContentDocumentCodec(CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
    }

    @Override
    public WriteContentDocument decode(BsonReader reader, DecoderContext decoderContext) {
        throw new UnsupportedOperationException("The WriteContentDocumentCodec can only encode to Bson");
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void encode(BsonWriter writer, WriteContentDocument value, EncoderContext encoderContext) {
        Object object = value.getEncodingObject();
        Codec codec = codecRegistry.get(object.getClass());
        encoderContext.encodeWithChildContext(codec, writer, object);
    }

    @Override
    public Class<WriteContentDocument> getEncoderClass() {
        return WriteContentDocument.class;
    }
}
