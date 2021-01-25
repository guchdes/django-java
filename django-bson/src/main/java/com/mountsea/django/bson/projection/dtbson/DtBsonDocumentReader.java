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

import org.bson.*;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A {@code BsonReader} implementation that reads from an instance of {@code DtBsonDocument}.  This can be used to decode a {@code
 * DtBsonDocument} using a {@code Decoder}.
 *
 * @see DtBsonDocument
 * @see org.bson.codecs.Decoder
 */
public class DtBsonDocumentReader extends AbstractBsonReader {
    private DtBsonValue currentValue;
    private Mark mark;

    /**
     * Construct a new instance.
     *
     * @param document the document to read from
     */
    public DtBsonDocumentReader(final DtBsonDocument document) {
        super();
        setContext(new Context(null, BsonContextType.TOP_LEVEL, document));
        currentValue = document;
    }

    @Override
    protected BsonBinary doReadBinaryData() {
        DtBsonBinary dtBsonBinary = currentValue.asBinary();
        return new BsonBinary(dtBsonBinary.getType(), dtBsonBinary.getData());
    }

    @Override
    protected byte doPeekBinarySubType() {
        return currentValue.asBinary().getType();
    }

    @Override
    protected int doPeekBinarySize() {
        return currentValue.asBinary().getData().length;
    }

    @Override
    protected boolean doReadBoolean() {
        return currentValue.asBoolean().getValue();
    }

    @Override
    protected long doReadDateTime() {
        return currentValue.asDateTime().getValue();
    }

    @Override
    protected double doReadDouble() {
        return currentValue.asDouble().getValue();
    }

    @Override
    protected void doReadEndArray() {
        setContext(getContext().getParentContext());
    }

    @Override
    protected void doReadEndDocument() {
        setContext(getContext().getParentContext());
        switch (getContext().getContextType()) {
            case ARRAY:
            case DOCUMENT:
                setState(State.TYPE);
                break;
            case TOP_LEVEL:
                setState(State.DONE);
                break;
            default:
                throw new BSONException("Unexpected ContextType.");
        }
    }

    @Override
    protected int doReadInt32() {
        return currentValue.asInt32().getValue();
    }

    @Override
    protected long doReadInt64() {
        return currentValue.asInt64().getValue();
    }

    @Override
    public Decimal128 doReadDecimal128() {
        return currentValue.asDecimal128().getValue();
    }

    @Override
    protected String doReadJavaScript() {
        return currentValue.asJavaScript().getCode();
    }

    @Override
    protected String doReadJavaScriptWithScope() {
        return currentValue.asJavaScriptWithScope().getCode();
    }

    @Override
    protected void doReadMaxKey() {
    }

    @Override
    protected void doReadMinKey() {
    }

    @Override
    protected void doReadNull() {
    }

    @Override
    protected ObjectId doReadObjectId() {
        return currentValue.asObjectId().getValue();
    }

    @Override
    protected BsonRegularExpression doReadRegularExpression() {
        DtBsonRegularExpression dtBsonRegularExpression = currentValue.asRegularExpression();
        return new BsonRegularExpression(dtBsonRegularExpression.getPattern(), dtBsonRegularExpression.getOptions());
    }

    @Override
    protected BsonDbPointer doReadDBPointer() {
        DtBsonDbPointer dtBsonDbPointer = currentValue.asDBPointer();
        return new BsonDbPointer(dtBsonDbPointer.getNamespace(), dtBsonDbPointer.getId());
    }

    @Override
    protected void doReadStartArray() {
        DtBsonArray dtBsonArray = currentValue.asArray();
        setContext(new Context(getContext(), BsonContextType.ARRAY, dtBsonArray));
    }

    @Override
    protected void doReadStartDocument() {
        DtBsonDocument document;
        if (currentValue.getBsonType() == BsonType.JAVASCRIPT_WITH_SCOPE) {
            document = currentValue.asJavaScriptWithScope().getScope();
        } else {
            document = currentValue.asDocument();
        }
        setContext(new Context(getContext(), BsonContextType.DOCUMENT, document));
    }

    @Override
    protected String doReadString() {
        return currentValue.asString().getValue();
    }

    @Override
    protected String doReadSymbol() {
        return currentValue.asSymbol().getSymbol();
    }

    @Override
    protected BsonTimestamp doReadTimestamp() {
        return new BsonTimestamp(currentValue.asTimestamp().getValue());
    }

    @Override
    protected void doReadUndefined() {
    }

    @Override
    protected void doSkipName() {
    }

    @Override
    protected void doSkipValue() {
    }

    @Override
    public BsonType readBsonType() {
        if (getState() == State.INITIAL || getState() == State.SCOPE_DOCUMENT) {
            // there is an implied type of Document for the top level and for scope documents
            setCurrentBsonType(BsonType.DOCUMENT);
            setState(State.VALUE);
            return getCurrentBsonType();
        }

        if (getState() != State.TYPE) {
            throwInvalidState("ReadBSONType", State.TYPE);
        }

        switch (getContext().getContextType()) {
            case ARRAY:
                currentValue = getContext().getNextValue();
                if (currentValue == null) {
                    setState(State.END_OF_ARRAY);
                    return BsonType.END_OF_DOCUMENT;
                }
                setState(State.VALUE);
                break;
            case DOCUMENT:
                Map.Entry<String, DtBsonValue> currentElement = getContext().getNextElement();
                if (currentElement == null) {
                    setState(State.END_OF_DOCUMENT);
                    return BsonType.END_OF_DOCUMENT;
                }
                setCurrentName(currentElement.getKey());
                currentValue = currentElement.getValue();
                setState(State.NAME);
                break;
            default:
                throw new BSONException("Invalid ContextType.");
        }

        setCurrentBsonType(currentValue.getBsonType());
        return getCurrentBsonType();
    }

    @Deprecated
    @Override
    public void mark() {
        if (mark != null) {
            throw new BSONException("A mark already exists; it needs to be reset before creating a new one");
        }
        mark = new Mark();
    }

    @Override
    public BsonReaderMark getMark() {
        return new Mark();
    }

    @Deprecated
    @Override
    public void reset() {
        if (mark == null) {
            throw new BSONException("trying to reset a mark before creating it");
        }
        mark.reset();
        mark = null;
    }

    @Override
    protected Context getContext() {
        return (Context) super.getContext();
    }

    protected class Mark extends AbstractBsonReader.Mark {
        private final DtBsonValue currentValue;
        private final Context context;

        protected Mark() {
            super();
            currentValue = DtBsonDocumentReader.this.currentValue;
            context = DtBsonDocumentReader.this.getContext();
            context.mark();
        }

        public void reset() {
            super.reset();
            DtBsonDocumentReader.this.currentValue = currentValue;
            DtBsonDocumentReader.this.setContext(context);
            context.reset();
        }
    }

    private static class DtBsonDocumentMarkableIterator<T> implements Iterator<T> {

        private Iterator<T> baseIterator;
        private List<T> markIterator = new ArrayList<T>();
        private int curIndex; // index of the cursor
        private boolean marking;

        protected DtBsonDocumentMarkableIterator(final Iterator<T> baseIterator) {
            this.baseIterator = baseIterator;
            curIndex = 0;
            marking = false;
        }

        /**
         *
         */
        protected void mark() {
            marking = true;
        }

        /**
         *
         */
        protected void reset() {
            curIndex = 0;
            marking = false;
        }


        @Override
        public boolean hasNext() {
            return baseIterator.hasNext() || curIndex < markIterator.size();
        }

        @Override
        public T next() {
            T value;
            //TODO: check closed
            if (curIndex < markIterator.size()) {
                value = markIterator.get(curIndex);
                if (marking) {
                    curIndex++;
                } else {
                    markIterator.remove(0);
                }
            } else {
                value = baseIterator.next();
                if (marking) {
                    markIterator.add(value);
                    curIndex++;
                }
            }


            return value;
        }

        @Override
        public void remove() {
            // iterator is read only
        }
    }

    protected class Context extends AbstractBsonReader.Context {

        private DtBsonDocumentMarkableIterator<Map.Entry<String, DtBsonValue>> documentIterator;
        private DtBsonDocumentMarkableIterator<DtBsonValue> arrayIterator;

        protected Context(final Context parentContext, final BsonContextType contextType, final DtBsonArray array) {
            super(parentContext, contextType);
            arrayIterator = new DtBsonDocumentMarkableIterator<>(array.iterator());
        }

        protected Context(final Context parentContext, final BsonContextType contextType, final DtBsonDocument document) {
            super(parentContext, contextType);
            documentIterator = new DtBsonDocumentMarkableIterator<>(document.entrySet().iterator());
        }

        public Map.Entry<String, DtBsonValue> getNextElement() {
            if (documentIterator.hasNext()) {
                return documentIterator.next();
            } else {
                return null;
            }
        }
        protected void mark() {
            if (documentIterator != null) {
                documentIterator.mark();
            } else {
                arrayIterator.mark();
            }

            if (getParentContext() != null) {
                ((Context) getParentContext()).mark();
            }
        }

        protected void reset() {
            if (documentIterator != null) {
                documentIterator.reset();
            } else {
                arrayIterator.reset();
            }

            if (getParentContext() != null) {
                ((Context) getParentContext()).reset();
            }
        }

        public DtBsonValue getNextValue() {
            if (arrayIterator.hasNext()) {
                return arrayIterator.next();
            } else {
                return null;
            }
        }

        public AbstractBsonReader.Context getParentContext() {
            return super.getParentContext();
        }

        public BsonContextType getContextType() {
            return super.getContextType();
        }
    }
}
