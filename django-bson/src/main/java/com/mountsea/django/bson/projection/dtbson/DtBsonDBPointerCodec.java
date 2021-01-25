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
package com.mountsea.django.bson.projection.dtbson;

import org.bson.BsonDbPointer;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Converts BSON type DBPointer(0x0c) to database references as DBPointer is deprecated.
 *
 * @since 3.0
 */
public class DtBsonDBPointerCodec implements Codec<DtBsonDbPointer> {

    @Override
    public DtBsonDbPointer decode(final BsonReader reader, final DecoderContext decoderContext) {
        BsonDbPointer bsonDbPointer = reader.readDBPointer();
        return new DtBsonDbPointer(bsonDbPointer.getNamespace(), bsonDbPointer.getId());
    }

    @Override
    public void encode(final BsonWriter writer, final DtBsonDbPointer value, final EncoderContext encoderContext) {
        BsonDbPointer bsonDbPointer = new BsonDbPointer(value.getNamespace(), value.getId());
        writer.writeDBPointer(bsonDbPointer);
    }

    @Override
    public Class<DtBsonDbPointer> getEncoderClass() {
        return DtBsonDbPointer.class;
    }
}
