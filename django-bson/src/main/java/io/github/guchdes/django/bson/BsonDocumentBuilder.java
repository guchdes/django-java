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

import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guch
 */
public class BsonDocumentBuilder {

    private final BsonDocument bsonDocument = new BsonDocument();

    private Map<String, Object> nonBsonValues;

    private BsonDocumentBuilder() {
    }

    public static BsonDocumentBuilder newBuilder() {
        return new BsonDocumentBuilder();
    }

    public static BsonDocumentBuilder newBuilder(String field, Object value) {
        BsonDocumentBuilder builder = newBuilder();
        builder.put(field, value);
        return builder;
    }

    public static BsonDocumentBuilder newBuilder(Map<String, ?> map) {
        BsonDocumentBuilder builder = newBuilder();
        builder.putAll(map);
        return builder;
    }

    public BsonDocument build() {
        if (nonBsonValues != null) {
            BsonDocument bsonDocument1 = BsonUtils.toBsonDocument(nonBsonValues);
            bsonDocument.putAll(bsonDocument1);
        }
        return bsonDocument;
    }

    public BsonDocumentBuilder put(String field, Object value) {
        if (value instanceof BsonValue) {
            bsonDocument.put(field, (BsonValue) value);
        } else {
            if (nonBsonValues == null) {
                nonBsonValues = new HashMap<>();
            }
            nonBsonValues.put(field, value);
        }
        return this;
    }

    public BsonDocumentBuilder putAll(Map<String, ?> map) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        return this;
    }
}
