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

import com.mountsea.django.bson.projection.DocumentList;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.codecs.DecoderContext;
import org.bson.json.JsonReader;

import java.util.Collections;
import java.util.List;

/**
 * A type-safe representation of the BSON array type.
 *
 * @since 3.0
 */
public class DtBsonArray extends DocumentList<DtBsonValue> implements DtBsonValue, Cloneable {

    /**
     * Construct an empty BsonArray
     */
    public DtBsonArray() {
        super(false);
    }

    public DtBsonArray(List<? extends DtBsonValue> list) {
        super(list, false);
    }

    public BsonArray toBsonArray() {
        DtBsonDocument dtBsonDocument = new DtBsonDocument("v", this);
        return dtBsonDocument.toBsonDocument().getArray("v");
    }

    public static DtBsonArray fromBsonArray(BsonArray bsonArray) {
        BsonDocument bsonDocument = new BsonDocument("v", bsonArray);
        return DtBsonDocument.fromBsonDocument(bsonDocument).getArray("v");
    }

    /**
     * Parses a string in MongoDB Extended JSON format to a {@code BsonArray}
     *
     * @param json the JSON string
     * @return a corresponding {@code BsonArray} object
     * @see JsonReader
     * @mongodb.driver.manual reference/mongodb-extended-json/ MongoDB Extended JSON
     *
     * @since 3.4
     */
    public static DtBsonArray parse(final String json) {
        return new DtBsonArrayCodec().decode(new JsonReader(json), DecoderContext.builder().build());
    }

    /**
     * Gets the values in this array as a list of {@code DtBsonValue} objects.
     *
     * @return the values in this array.
     */
    public List<DtBsonValue> getValues() {
        return Collections.unmodifiableList(this);
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.ARRAY;
    }

    @Override
    public String toString() {
        return "DtBsonArray{"
               + "values=" + super.toString()
               + '}';
    }

    @Override
    @SuppressWarnings("unchecked")
    public DtBsonArray deepCloneSelf() {
        return clone();
    }

    @Override
    public DtBsonArray clone() {
        DtBsonArray to = new DtBsonArray();
        for (DtBsonValue cur : this) {
            switch (cur.getBsonType()) {
                case DOCUMENT:
                    to.add(cur.asDocument().clone());
                    break;
                case ARRAY:
                    to.add(cur.asArray().clone());
                    break;
                case BINARY:
                    to.add(DtBsonBinary.clone(cur.asBinary()));
                    break;
                case JAVASCRIPT_WITH_SCOPE:
                    to.add(DtBsonJavaScriptWithScope.clone(cur.asJavaScriptWithScope()));
                    break;
                default:
                    to.add(cur);
            }
        }
        return to;
    }
}
