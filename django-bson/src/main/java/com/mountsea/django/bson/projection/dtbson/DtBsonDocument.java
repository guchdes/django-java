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

import com.mountsea.django.bson.BsonUtils;
import com.mountsea.django.bson.projection.LinkedDocumentMap;
import org.bson.BSONException;
import org.bson.BsonDocument;
import org.bson.BsonInvalidOperationException;
import org.bson.BsonType;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

import java.io.StringWriter;
import java.util.List;

import static java.lang.String.format;

/**
 * A type-safe container for a BSON document.  This class should NOT be sub-classed by third parties.
 *
 * @see BsonUtils#toDtBsonDocument(Object)
 * @see BsonUtils#fromDtBsonDocument(DtBsonDocument, Class)
 */
public class DtBsonDocument extends LinkedDocumentMap<String, DtBsonValue> implements DtBsonValue, Cloneable {

    /**
     * Construct a new instance with the given list {@code BsonElement}, none of which may be null.
     *
     * @param bsonElements a list of {@code BsonElement}
     */
    public DtBsonDocument(final List<DtBsonElement> bsonElements) {
        this();
        for (DtBsonElement cur : bsonElements) {
            put(cur.getName(), cur.getValue());
        }
    }

    /**
     * Construct a new instance with a single key value pair
     *
     * @param key   the key
     * @param value the value
     */
    public DtBsonDocument(final String key, final DtBsonValue value) {
        this();
        put(key, value);
    }

    /**
     * Construct an empty document.
     */
    public DtBsonDocument() {
        super(false);
    }


    public BsonDocument toBsonDocument() {
        return BsonUtils.toBsonDocument(this);
    }

    public static DtBsonDocument fromBsonDocument(BsonDocument bsonDocument) {
        return BsonUtils.fromBsonDocument(bsonDocument, DtBsonDocument.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DtBsonDocument deepCloneSelf() {
        return this.clone();
    }

    /**
     * Parses a string in MongoDB Extended JSON format to a {@code DtBsonDocument}
     *
     * @param json the JSON string
     * @return a corresponding {@code DtBsonDocument} object
     * @mongodb.driver.manual reference/mongodb-extended-json/ MongoDB Extended JSON
     * @see JsonReader
     */
    public static DtBsonDocument parse(final String json) {
        return new DtBsonDocumentCodec().decode(new JsonReader(json), DecoderContext.builder().build());
    }


    @Override
    public BsonType getBsonType() {
        return BsonType.DOCUMENT;
    }

    /**
     * Gets the value of the key if it is a BsonDocument, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a BsonDocument
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not a BsonDocument
     */
    public DtBsonDocument getDocument(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asDocument();
    }

    /**
     * Gets the value of the key if it is a DtBsonArray, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonArray
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonArray getArray(final Object key) {
        throwIfKeyAbsent(key);

        return get(key).asArray();
    }

    /**
     * Gets the value of the key if it is a DtBsonNumber, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonNumber
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonNumber getNumber(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asNumber();
    }

    /**
     * Gets the value of the key if it is a DtBsonInt32, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonInt32
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonInt32 getInt32(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asInt32();
    }

    /**
     * Gets the value of the key if it is a DtBsonInt64, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonInt64
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonInt64 getInt64(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asInt64();
    }

    /**
     * Gets the value of the key if it is a DtBsonDecimal128, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonDecimal128
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     * @since 3.4
     */
    public DtBsonDecimal128 getDecimal128(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asDecimal128();
    }

    /**
     * Gets the value of the key if it is a DtBsonDouble, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonDouble
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonDouble getDouble(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asDouble();
    }

    /**
     * Gets the value of the key if it is a DtBsonBoolean, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonBoolean
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonBoolean getBoolean(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asBoolean();
    }

    /**
     * Gets the value of the key if it is a DtBsonString, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonString
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonString getString(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asString();
    }

    /**
     * Gets the value of the key if it is a DtBsonDateTime, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonDateTime
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonDateTime getDateTime(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asDateTime();
    }

    /**
     * Gets the value of the key if it is a DtBsonTimestamp, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonTimestamp
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonTimestamp getTimestamp(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asTimestamp();
    }

    /**
     * Gets the value of the key if it is a DtBsonObjectId, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonObjectId
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonObjectId getObjectId(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asObjectId();
    }

    /**
     * Gets the value of the key if it is a DtBsonRegularExpression, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonRegularExpression
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonRegularExpression getRegularExpression(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asRegularExpression();
    }

    /**
     * Gets the value of the key if it is a DtBsonBinary, or throws if not.
     *
     * @param key the key
     * @return the value of the key as a DtBsonBinary
     * @throws BsonInvalidOperationException if the document does not contain the key or the value is not of the expected type
     */
    public DtBsonBinary getBinary(final Object key) {
        throwIfKeyAbsent(key);
        return get(key).asBinary();
    }

    /**
     * Returns true if the value of the key is a DtBsonNull, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonNull, returns false if the document does not contain the key.
     */
    public boolean isNull(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isNull();
    }

    /**
     * Returns true if the value of the key is a DtBsonDocument, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonDocument, returns false if the document does not contain the key.
     */
    public boolean isDocument(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isDocument();
    }

    /**
     * Returns true if the value of the key is a DtBsonArray, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonArray, returns false if the document does not contain the key.
     */
    public boolean isArray(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isArray();
    }

    /**
     * Returns true if the value of the key is a DtBsonNumber, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonNumber, returns false if the document does not contain the key.
     */
    public boolean isNumber(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isNumber();
    }

    /**
     * Returns true if the value of the key is a DtBsonInt32, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonInt32, returns false if the document does not contain the key.
     */
    public boolean isInt32(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isInt32();
    }

    /**
     * Returns true if the value of the key is a DtBsonInt64, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonInt64, returns false if the document does not contain the key.
     */
    public boolean isInt64(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isInt64();
    }

    /**
     * Returns true if the value of the key is a DtBsonDecimal128, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonDecimal128, returns false if the document does not contain the key.
     * @since 3.4
     */
    public boolean isDecimal128(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isDecimal128();
    }


    /**
     * Returns true if the value of the key is a DtBsonDouble, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonDouble, returns false if the document does not contain the key.
     */
    public boolean isDouble(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isDouble();
    }

    /**
     * Returns true if the value of the key is a DtBsonBoolean, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonBoolean, returns false if the document does not contain the key.
     */
    public boolean isBoolean(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isBoolean();
    }

    /**
     * Returns true if the value of the key is a DtBsonString, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonString, returns false if the document does not contain the key.
     */
    public boolean isString(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isString();
    }

    /**
     * Returns true if the value of the key is a DtBsonDateTime, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonDateTime, returns false if the document does not contain the key.
     */
    public boolean isDateTime(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isDateTime();
    }

    /**
     * Returns true if the value of the key is a DtBsonTimestamp, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonTimestamp, returns false if the document does not contain the key.
     */
    public boolean isTimestamp(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isTimestamp();
    }

    /**
     * Returns true if the value of the key is a DtBsonObjectId, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonObjectId, returns false if the document does not contain the key.
     */
    public boolean isObjectId(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isObjectId();
    }

    /**
     * Returns true if the value of the key is a DtBsonBinary, returns false if the document does not contain the key.
     *
     * @param key the key
     * @return true if the value of the key is a DtBsonBinary, returns false if the document does not contain the key.
     */
    public boolean isBinary(final Object key) {
        if (!containsKey(key)) {
            return false;
        }
        return get(key).isBinary();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonValue
     */
    public DtBsonValue get(final Object key, final DtBsonValue defaultValue) {
        DtBsonValue value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonDocument.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonDocument
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonDocument getDocument(final Object key, final DtBsonDocument defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asDocument();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonArray.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonArray
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonArray getArray(final Object key, final DtBsonArray defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asArray();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonNumber.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonNumber
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonNumber getNumber(final Object key, final DtBsonNumber defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asNumber();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonInt32.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonInt32
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonInt32 getInt32(final Object key, final DtBsonInt32 defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asInt32();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonInt64.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonInt64
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonInt64 getInt64(final Object key, final DtBsonInt64 defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asInt64();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonDecimal128.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonDecimal128
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     * @since 3.4
     */
    public DtBsonDecimal128 getDecimal128(final Object key, final DtBsonDecimal128 defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asDecimal128();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonDouble.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonDouble
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonDouble getDouble(final Object key, final DtBsonDouble defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asDouble();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonBoolean.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonBoolean
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonBoolean getBoolean(final Object key, final DtBsonBoolean defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asBoolean();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonString.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonString
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonString getString(final Object key, final DtBsonString defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asString();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonDateTime.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonDateTime
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonDateTime getDateTime(final Object key, final DtBsonDateTime defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asDateTime();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonTimestamp.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonTimestamp
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonTimestamp getTimestamp(final Object key, final DtBsonTimestamp defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asTimestamp();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonObjectId.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonObjectId
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonObjectId getObjectId(final Object key, final DtBsonObjectId defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asObjectId();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonBinary.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonBinary
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonBinary getBinary(final Object key, final DtBsonBinary defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asBinary();
    }

    /**
     * If the document does not contain the given key, return the given default value.  Otherwise, gets the value of the key as a
     * DtBsonRegularExpression.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value of the key as a DtBsonRegularExpression
     * @throws BsonInvalidOperationException if the document contains the key but the value is not of the expected type
     */
    public DtBsonRegularExpression getRegularExpression(final Object key, final DtBsonRegularExpression defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        return get(key).asRegularExpression();
    }

    @Override
    public DtBsonValue put(final String key, final DtBsonValue value) {
        if (value == null) {
            throw new IllegalArgumentException(format("The value for key %s can not be null", key));
        }
        if (key.contains("\0")) {
            throw new BSONException(format("BSON cstring '%s' is not valid because it contains a null character at index %d", key,
                    key.indexOf('\0')));
        }
        return super.put(key, value);
    }

    /**
     * Put the given key and value into this document, and return the document.
     *
     * @param key   the key
     * @param value the value
     * @return this
     */
    public DtBsonDocument append(final String key, final DtBsonValue value) {
        put(key, value);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DtBsonDocument)) {
            return false;
        }

        DtBsonDocument that = (DtBsonDocument) o;

        return entrySet().equals(that.entrySet());
    }

    /**
     * Gets a JSON representation of this document using the given {@code JsonWriterSettings}.
     *
     * @param settings the JSON writer settings
     * @return a JSON representation of this document
     */
    public String toJson(final JsonWriterSettings settings) {
        StringWriter writer = new StringWriter();
        new DtBsonDocumentCodec().encode(new JsonWriter(writer, settings), this, EncoderContext.builder().build());
        return writer.toString();
    }

    @Override
    public String toString() {
        return toJson(JsonWriterSettings.builder().build());
    }

    @Override
    public DtBsonDocument clone() {
        DtBsonDocument to = new DtBsonDocument();
        for (Entry<String, DtBsonValue> cur : entrySet()) {
            switch (cur.getValue().getBsonType()) {
                case DOCUMENT:
                    to.put(cur.getKey(), cur.getValue().asDocument().clone());
                    break;
                case ARRAY:
                    to.put(cur.getKey(), cur.getValue().asArray().clone());
                    break;
                case BINARY:
                    to.put(cur.getKey(), DtBsonBinary.clone(cur.getValue().asBinary()));
                    break;
                case JAVASCRIPT_WITH_SCOPE:
                    to.put(cur.getKey(), DtBsonJavaScriptWithScope.clone(cur.getValue().asJavaScriptWithScope()));
                    break;
                default:
                    to.put(cur.getKey(), cur.getValue());
            }
        }
        return to;
    }

    private void throwIfKeyAbsent(final Object key) {
        if (!containsKey(key)) {
            throw new BsonInvalidOperationException("Document does not contain key " + key);
        }
    }

}
