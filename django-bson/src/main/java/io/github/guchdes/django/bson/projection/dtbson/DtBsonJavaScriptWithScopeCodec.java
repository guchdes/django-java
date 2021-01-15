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

import org.bson.BsonDocument;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * A Codec for {@code DtBsonJavaScriptWithScope} instances.
 *
 * @since 3.0
 */
public class DtBsonJavaScriptWithScopeCodec implements Codec<DtBsonJavaScriptWithScope> {
    private final Codec<DtBsonDocument> documentCodec;

    /**
     * Construct a new instance with the given codec to use for the nested document
     *
     * @param documentCodec the non-null codec for the nested document
     */
    public DtBsonJavaScriptWithScopeCodec(final Codec<DtBsonDocument> documentCodec) {
        this.documentCodec = documentCodec;
    }

    @Override
    public DtBsonJavaScriptWithScope decode(final BsonReader bsonReader, final DecoderContext decoderContext) {
        String code = bsonReader.readJavaScriptWithScope();
        DtBsonDocument scope = documentCodec.decode(bsonReader, decoderContext);
        return new DtBsonJavaScriptWithScope(code, scope);
    }

    @Override
    public void encode(final BsonWriter writer, final DtBsonJavaScriptWithScope codeWithScope, final EncoderContext encoderContext) {
        writer.writeJavaScriptWithScope(codeWithScope.getCode());
        documentCodec.encode(writer, codeWithScope.getScope(), encoderContext);
    }

    @Override
    public Class<DtBsonJavaScriptWithScope> getEncoderClass() {
        return DtBsonJavaScriptWithScope.class;
    }
}
