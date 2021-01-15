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
package io.github.guchdes.django.bson.projection.dtbson;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * <p>Allows reading and writing of the BSON Undefined type.  On encoding, it will write the correct type to the BsonWriter, but ignore the
 * value, and on decoding it will read the type off the BsonReader and return an Undefined type, which simply represents a placeholder for
 * the undefined value.</p>
 *
 * <p>The undefined type is deprecated (see the spec).</p>
 *
 * @see <a href="http://bsonspec.org/spec.html">BSON Spec</a>
 * @see org.bson.BsonType#UNDEFINED
 * @since 3.0
 */
public class DtBsonUndefinedCodec implements Codec<DtBsonUndefined> {
    @Override
    public DtBsonUndefined decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readUndefined();
        return new DtBsonUndefined();
    }

    @Override
    public void encode(final BsonWriter writer, final DtBsonUndefined value, final EncoderContext encoderContext) {
        writer.writeUndefined();
    }

    @Override
    public Class<DtBsonUndefined> getEncoderClass() {
        return DtBsonUndefined.class;
    }
}
