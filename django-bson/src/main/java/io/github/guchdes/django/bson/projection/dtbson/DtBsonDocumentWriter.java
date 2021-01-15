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

import org.bson.*;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import static org.bson.BsonContextType.DOCUMENT;
import static org.bson.BsonContextType.SCOPE_DOCUMENT;

/**
 * A {@code BsonWriter} implementation that writes to an instance of {@code DtBsonDocument}.  This can be used to encode an object into a
 * {@code DtBsonDocument} using an {@code Encoder}.
 *
 * @see BsonDocument
 * @see org.bson.codecs.Encoder
 */
public class DtBsonDocumentWriter extends AbstractBsonWriter {

    private final DtBsonDocument document;

    /**
     * Construct a new instance.
     *
     * @param document the document to write to
     */
    public DtBsonDocumentWriter(final DtBsonDocument document) {
        super(new BsonWriterSettings());
        this.document = document;
        setContext(new Context());
    }

    /**
     * Gets the document that the writer is writing to.
     *
     * @return the document
     */
    public DtBsonDocument getDocument() {
        return document;
    }

    @Override
    protected void doWriteStartDocument() {
        switch (getState()) {
            case INITIAL:
                setContext(new Context(document, DOCUMENT, getContext()));
                break;
            case VALUE:
                setContext(new Context(new DtBsonDocument(), DOCUMENT, getContext()));
                break;
            case SCOPE_DOCUMENT:
                setContext(new Context(new DtBsonDocument(), SCOPE_DOCUMENT, getContext()));
                break;
            default:
                throw new BsonInvalidOperationException("Unexpected state " + getState());
        }
    }

    @Override
    protected void doWriteEndDocument() {
        DtBsonValue value = getContext().container;
        setContext(getContext().getParentContext());

        if (getContext().getContextType() == BsonContextType.JAVASCRIPT_WITH_SCOPE) {
            DtBsonDocument scope = (DtBsonDocument) value;
            DtBsonString code = (DtBsonString) getContext().container;
            setContext(getContext().getParentContext());
            write(new DtBsonJavaScriptWithScope(code.getValue(), scope));
        } else if (getContext().getContextType() != BsonContextType.TOP_LEVEL) {
            write(value);
        }
    }

    @Override
    protected void doWriteStartArray() {
        setContext(new Context(new DtBsonArray(), BsonContextType.ARRAY, getContext()));
    }

    @Override
    protected void doWriteEndArray() {
        DtBsonValue array = getContext().container;
        setContext(getContext().getParentContext());
        write(array);
    }

    @Override
    protected void doWriteBinaryData(final BsonBinary value) {
        write(new DtBsonBinary(value.getType(), value.getData()));
    }

    @Override
    public void doWriteBoolean(final boolean value) {
        write(DtBsonBoolean.valueOf(value));
    }

    @Override
    protected void doWriteDateTime(final long value) {
        write(new DtBsonDateTime(value));
    }

    @Override
    protected void doWriteDBPointer(final BsonDbPointer value) {
        write(new DtBsonDbPointer(value.getNamespace(), value.getId()));
    }

    @Override
    protected void doWriteDouble(final double value) {
        write(new DtBsonDouble(value));
    }

    @Override
    protected void doWriteInt32(final int value) {
        write(new DtBsonInt32(value));
    }

    @Override
    protected void doWriteInt64(final long value) {
        write(new DtBsonInt64(value));
    }

    @Override
    protected void doWriteDecimal128(final Decimal128 value) {
        write(new DtBsonDecimal128(value));
    }

    @Override
    protected void doWriteJavaScript(final String value) {
        write(new DtBsonJavaScript(value));
    }

    @Override
    protected void doWriteJavaScriptWithScope(final String value) {
        setContext(new Context(new DtBsonString(value), BsonContextType.JAVASCRIPT_WITH_SCOPE, getContext()));
    }

    @Override
    protected void doWriteMaxKey() {
        write(new DtBsonMaxKey());
    }

    @Override
    protected void doWriteMinKey() {
        write(new DtBsonMinKey());
    }

    @Override
    public void doWriteNull() {
        write(DtBsonNull.VALUE);
    }

    @Override
    public void doWriteObjectId(final ObjectId value) {
        write(new DtBsonObjectId(value));
    }

    @Override
    public void doWriteRegularExpression(final BsonRegularExpression value) {
        write(new DtBsonRegularExpression(value.getPattern(), value.getOptions()));
    }

    @Override
    public void doWriteString(final String value) {
        write(new DtBsonString(value));
    }

    @Override
    public void doWriteSymbol(final String value) {
        write(new DtBsonSymbol(value));
    }

    @Override
    public void doWriteTimestamp(final BsonTimestamp value) {
        write(new DtBsonTimestamp(value.getValue()));
    }

    @Override
    public void doWriteUndefined() {
        write(new DtBsonUndefined());
    }

    @Override
    public void flush() {
    }

    @Override
    protected Context getContext() {
        return (Context) super.getContext();
    }

    private void write(final DtBsonValue value) {
        getContext().add(value);
    }

    private class Context extends AbstractBsonWriter.Context {
        private DtBsonValue container;

        Context(final DtBsonValue container, final BsonContextType contextType, final Context parent) {
            super(parent, contextType);
            this.container = container;
        }

        Context() {
            super(null, BsonContextType.TOP_LEVEL);
        }

        void add(final DtBsonValue value) {
            if (container instanceof DtBsonArray) {
                ((DtBsonArray) container).add(value);
            } else {
                ((DtBsonDocument) container).put(getName(), value);
            }
        }
    }
}
