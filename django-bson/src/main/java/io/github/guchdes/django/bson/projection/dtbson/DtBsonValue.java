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

import io.github.guchdes.django.bson.BsonUtils;
import org.bson.*;

import static java.lang.String.format;

/**
 * 表示可以在 {@link DtBsonDocument}/{@link DtBsonArray}以及DocumentNode子类的字段中使用的类型.
 * <p>
 * Dt系列类型是Document类型系统中实现弱类型的方式。
 *
 * @see BsonUtils#toDtBsonDocument(Object)
 * @see BsonUtils#fromDtBsonDocument(DtBsonDocument, Class)
 */
public interface DtBsonValue {

    /**
     * Gets the BSON type of this value.
     *
     * @return the BSON type, which may not be null (but may be BSONType.NULL)
     */
    BsonType getBsonType();

    /**
     * Gets this value as a DtBsonDocument if it is one, otherwise throws exception
     *
     * @return a DtBsonDocument
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonDocument asDocument() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.DOCUMENT);
        return (DtBsonDocument) this;
    }

    /**
     * Gets this value as a DtBsonArray if it is one, otherwise throws exception
     *
     * @return a DtBsonArray
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonArray asArray() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.ARRAY);
        return (DtBsonArray) this;
    }

    /**
     * Gets this value as a DtBsonString if it is one, otherwise throws exception
     *
     * @return a DtBsonString
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonString asString() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.STRING);
        return (DtBsonString) this;
    }

    /**
     * Gets this value as a DtBsonNumber if it is one, otherwise throws exception
     *
     * @return a DtBsonNumber
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonNumber asNumber() {
        if (getBsonType() != BsonType.INT32 && getBsonType() != BsonType.INT64 && getBsonType() != BsonType.DOUBLE) {
            throw new BsonInvalidOperationException(format("Value expected to be of a numerical BSON type is of unexpected type %s",
                    getBsonType()));
        }
        return (DtBsonNumber) this;
    }

    /**
     * Gets this value as a DtBsonInt32 if it is one, otherwise throws exception
     *
     * @return a DtBsonInt32
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonInt32 asInt32() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.INT32);
        return (DtBsonInt32) this;
    }

    /**
     * Gets this value as a DtBsonInt64 if it is one, otherwise throws exception
     *
     * @return a DtBsonInt64
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonInt64 asInt64() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.INT64);
        return (DtBsonInt64) this;
    }

    /**
     * Gets this value as a DtBsonDecimal128 if it is one, otherwise throws exception
     *
     * @return a DtBsonDecimal128
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     * @since 3.4
     */
    default DtBsonDecimal128 asDecimal128() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.DECIMAL128);
        return (DtBsonDecimal128) this;
    }

    /**
     * Gets this value as a DtBsonDouble if it is one, otherwise throws exception
     *
     * @return a DtBsonDouble
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonDouble asDouble() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.DOUBLE);
        return (DtBsonDouble) this;
    }

    /**
     * Gets this value as a DtBsonBoolean if it is one, otherwise throws exception
     *
     * @return a DtBsonBoolean
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonBoolean asBoolean() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.BOOLEAN);
        return (DtBsonBoolean) this;
    }

    /**
     * Gets this value as an DtBsonObjectId if it is one, otherwise throws exception
     *
     * @return an DtBsonObjectId
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonObjectId asObjectId() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.OBJECT_ID);
        return (DtBsonObjectId) this;
    }

    /**
     * Gets this value as a DtBsonDbPointer if it is one, otherwise throws exception
     *
     * @return an DtBsonDbPointer
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonDbPointer asDBPointer() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.DB_POINTER);
        return (DtBsonDbPointer) this;
    }

    /**
     * Gets this value as a DtBsonTimestamp if it is one, otherwise throws exception
     *
     * @return an DtBsonTimestamp
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonTimestamp asTimestamp() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.TIMESTAMP);
        return (DtBsonTimestamp) this;
    }

    /**
     * Gets this value as a DtBsonBinary if it is one, otherwise throws exception
     *
     * @return an DtBsonBinary
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonBinary asBinary() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.BINARY);
        return (DtBsonBinary) this;
    }

    /**
     * Gets this value as a DtBsonDateTime if it is one, otherwise throws exception
     *
     * @return an DtBsonDateTime
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonDateTime asDateTime() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.DATE_TIME);
        return (DtBsonDateTime) this;
    }

    /**
     * Gets this value as a DtBsonSymbol if it is one, otherwise throws exception
     *
     * @return an DtBsonSymbol
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonSymbol asSymbol() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.SYMBOL);
        return (DtBsonSymbol) this;
    }

    /**
     * Gets this value as a DtBsonRegularExpression if it is one, otherwise throws exception
     *
     * @return an DtBsonRegularExpression
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonRegularExpression asRegularExpression() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.REGULAR_EXPRESSION);
        return (DtBsonRegularExpression) this;
    }

    /**
     * Gets this value as a {@code DtBsonJavaScript} if it is one, otherwise throws exception
     *
     * @return a DtBsonJavaScript
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonJavaScript asJavaScript() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.JAVASCRIPT);
        return (DtBsonJavaScript) this;
    }

    /**
     * Gets this value as a DtBsonJavaScriptWithScope if it is one, otherwise throws exception
     *
     * @return a DtBsonJavaScriptWithScope
     * @throws org.bson.BsonInvalidOperationException if this value is not of the expected type
     */
    default DtBsonJavaScriptWithScope asJavaScriptWithScope() {
        DtBsonHelper.throwIfInvalidType(this, BsonType.JAVASCRIPT_WITH_SCOPE);
        return (DtBsonJavaScriptWithScope) this;
    }


    /**
     * Returns true if this is a DtBsonNull, false otherwise.
     *
     * @return true if this is a DtBsonNull, false otherwise
     */
    default boolean isNull() {
        return this instanceof DtBsonNull;
    }

    /**
     * Returns true if this is a DtBsonDocument, false otherwise.
     *
     * @return true if this is a DtBsonDocument, false otherwise
     */
    default boolean isDocument() {
        return this instanceof DtBsonDocument;
    }

    /**
     * Returns true if this is a DtBsonArray, false otherwise.
     *
     * @return true if this is a DtBsonArray, false otherwise
     */
    default boolean isArray() {
        return this instanceof DtBsonArray;
    }

    /**
     * Returns true if this is a DtBsonString, false otherwise.
     *
     * @return true if this is a DtBsonString, false otherwise
     */
    default boolean isString() {
        return this instanceof DtBsonString;
    }

    /**
     * Returns true if this is a DtBsonNumber, false otherwise.
     *
     * @return true if this is a DtBsonNumber, false otherwise
     */
    default boolean isNumber() {
        return isInt32() || isInt64() || isDouble();
    }

    /**
     * Returns true if this is a DtBsonInt32, false otherwise.
     *
     * @return true if this is a DtBsonInt32, false otherwise
     */
    default boolean isInt32() {
        return this instanceof DtBsonInt32;
    }

    /**
     * Returns true if this is a DtBsonInt64, false otherwise.
     *
     * @return true if this is a DtBsonInt64, false otherwise
     */
    default boolean isInt64() {
        return this instanceof DtBsonInt64;
    }

    /**
     * Returns true if this is a DtBsonDecimal128, false otherwise.
     *
     * @return true if this is a DtBsonDecimal128, false otherwise
     * @since 3.4
     */
    default boolean isDecimal128() {
        return this instanceof DtBsonDecimal128;
    }

    /**
     * Returns true if this is a DtBsonDouble, false otherwise.
     *
     * @return true if this is a DtBsonDouble, false otherwise
     */
    default boolean isDouble() {
        return this instanceof DtBsonDouble;

    }

    /**
     * Returns true if this is a DtBsonBoolean, false otherwise.
     *
     * @return true if this is a DtBsonBoolean, false otherwise
     */
    default boolean isBoolean() {
        return this instanceof DtBsonBoolean;

    }

    /**
     * Returns true if this is an DtBsonObjectId, false otherwise.
     *
     * @return true if this is an DtBsonObjectId, false otherwise
     */
    default boolean isObjectId() {
        return this instanceof DtBsonObjectId;
    }

    /**
     * Returns true if this is a DtBsonDbPointer, false otherwise.
     *
     * @return true if this is a DtBsonDbPointer, false otherwise
     */
    default boolean isDBPointer() {
        return this instanceof DtBsonDbPointer;
    }

    /**
     * Returns true if this is a DtBsonTimestamp, false otherwise.
     *
     * @return true if this is a DtBsonTimestamp, false otherwise
     */
    default boolean isTimestamp() {
        return this instanceof DtBsonTimestamp;
    }

    /**
     * Returns true if this is a DtBsonBinary, false otherwise.
     *
     * @return true if this is a DtBsonBinary, false otherwise
     */
    default boolean isBinary() {
        return this instanceof DtBsonBinary;
    }

    /**
     * Returns true if this is a DtBsonDateTime, false otherwise.
     *
     * @return true if this is a DtBsonDateTime, false otherwise
     */
    default boolean isDateTime() {
        return this instanceof DtBsonDateTime;
    }

    /**
     * Returns true if this is a DtBsonSymbol, false otherwise.
     *
     * @return true if this is a DtBsonSymbol, false otherwise
     */
    default boolean isSymbol() {
        return this instanceof DtBsonSymbol;
    }

    /**
     * Returns true if this is a DtBsonRegularExpression, false otherwise.
     *
     * @return true if this is a DtBsonRegularExpression, false otherwise
     */
    default boolean isRegularExpression() {
        return this instanceof DtBsonRegularExpression;
    }

    /**
     * Returns true if this is a DtBsonJavaScript, false otherwise.
     *
     * @return true if this is a DtBsonJavaScript, false otherwise
     */
    default boolean isJavaScript() {
        return this instanceof DtBsonJavaScript;
    }

    /**
     * Returns true if this is a DtBsonJavaScriptWithScope, false otherwise.
     *
     * @return true if this is a DtBsonJavaScriptWithScope, false otherwise
     */
    default boolean isJavaScriptWithScope() {
        return this instanceof DtBsonJavaScriptWithScope;
    }
}
