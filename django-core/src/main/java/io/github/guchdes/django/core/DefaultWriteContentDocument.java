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
package io.github.guchdes.django.core;

import io.github.guchdes.django.bson.BsonUtils;
import io.github.guchdes.django.bson.util.InternalUtils;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.function.BiConsumer;

import static io.github.guchdes.django.core.WriteContentDocument.Mode.INSERT;
import static io.github.guchdes.django.core.WriteContentDocument.Mode.UPDATE;

/**
 * 文档初始为CollectibleDocument或Bson对象，如果有字段操作需求时，转换为Document对象，
 * 如果没有字段操作的需求，则可以避免多余的复制过程。
 *
 * @Author guch
 * @Since 3.0.0
 */
class DefaultWriteContentDocument implements WriteContentDocument {

    /**
     * 以下3个字段中只有一个不为null
     */
    Object object;

    Document document;

    BsonDocument bsonDocument;

    final CodecRegistry codecRegistry;

    final Mode mode;

    public static DefaultWriteContentDocument createInsert(Object object, CodecRegistry codecRegistry) {
        DefaultWriteContentDocument document = new DefaultWriteContentDocument(codecRegistry, INSERT);
        document.object = object;
        return document;
    }

    public static DefaultWriteContentDocument createUpdate(Object object, CodecRegistry codecRegistry) {
        DefaultWriteContentDocument document = new DefaultWriteContentDocument(codecRegistry, UPDATE);
        document.document = new Document(SET_OPERATOR, object);
        return document;
    }

    public static DefaultWriteContentDocument createUpdate(Bson bson, CodecRegistry codecRegistry) {
        DefaultWriteContentDocument document = new DefaultWriteContentDocument(codecRegistry, UPDATE);
        //Bson编码时需要先转换BsonDocument，可以提前此过程
        document.bsonDocument = BsonUtils.toBsonDocument(bson, codecRegistry);
        return document;
    }

    private DefaultWriteContentDocument(CodecRegistry codecRegistry, Mode mode) {
        this.codecRegistry = codecRegistry;
        this.mode = mode;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    public Bson getAsBson() {
        convertToDocument();
        return document == null ? bsonDocument : document;
    }

    @Override
    public Object getEncodingObject() {
        if (object != null) {
            return object;
        }
        if (document != null) {
            return document;
        }
        if (bsonDocument != null) {
            return bsonDocument;
        }
        throw new IllegalStateException();
    }

    public Object remove(String field) {
        if (mode == INSERT) {
            convertToDocument();
            if (document != null) {
                return document.remove(field);
            } else {
                return bsonDocument.remove(field);
            }
        } else {
            return evictOperate(field);
        }
    }

    public void put(String field, Object value) {
        if (mode == INSERT) {
            convertToDocument();
            if (document != null) {
                document.put(field, value);
            } else {
                putValueToBsonDocument(field, value, this.bsonDocument);
            }
        } else {
            convertToBsonDocument();
            evictOperate(field);
            BsonDocument set = null;
            BsonValue bsonValue = bsonDocument.get(SET_OPERATOR);
            if (bsonValue == null) {
                BsonDocument bsonDocument = new BsonDocument();
                this.bsonDocument.put(SET_OPERATOR, bsonDocument);
                set = bsonDocument;
            } else {
                set = bsonValue.asDocument();
            }
            putValueToBsonDocument(field, value, set);
        }
    }

    @Override
    public boolean contains(String field) {
        if (mode == INSERT) {
            convertToDocument();
            if (document != null) {
                return document.containsKey(field);
            } else {
                return bsonDocument.containsKey(field);
            }
        } else {
            return hasOperate(field);
        }
    }

    @Override
    public boolean isEmpty() {
        convertToDocument();
        if (document != null) {
            return document.isEmpty();
        } else {
            return bsonDocument.isEmpty();
        }
    }

    /**
     * 从CollectibleDocument转换到Document，执行浅复制。
     * 如果已经是BsonDocument状态，则调用没有影响，转换后仍然是BsonDocument状态
     */
    private void convertToDocument() {
        if (object != null) {
            Document document = new Document();
            CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(object.getClass());
            for (Map.Entry<String, CollectibleDocumentDefinition.Property<?>> entry : definition.getPropertyMap().entrySet()) {
                Object v = entry.getValue().get(object);
                if (v != null) {
                    document.put(entry.getKey(), v);
                }
            }
            this.document = document;
            this.object = null;
        }
    }

    /**
     * 转换到BsonDocument，执行深复制
     */
    private void convertToBsonDocument() {
        if (bsonDocument == null) {
            if (object != null) {
                this.bsonDocument = BsonUtils.toBsonDocument(object, codecRegistry);
                this.object = null;
            } else {
                this.bsonDocument = BsonUtils.toBsonDocument(document, codecRegistry);
                this.document = null;
            }
        }
    }

    private void putValueToBsonDocument(String field, Object o, BsonDocument bsonDocument) {
        if (o instanceof BsonValue) {
            bsonDocument.put(field, (BsonValue) o);
        } else {
            Document document = new Document(field, o);
            BsonDocument d = BsonUtils.toBsonDocument(document, codecRegistry);
            bsonDocument.putAll(d);
        }
    }

    private Object evictOperate(String field) {
        Object[] ret = new Object[1];
        traverseAllOperate((s, bsonDocument) -> {
            Iterator<Map.Entry<String, BsonValue>> fieldIterator = bsonDocument.entrySet().iterator();
            while (fieldIterator.hasNext()) {
                Map.Entry<String, BsonValue> next = fieldIterator.next();
                String fieldPath = next.getKey();
                if (InternalUtils.isEqOrSubPath(field, fieldPath)) {
                    if (ret[0] == null || s.equals(SET_OPERATOR)) {
                        ret[0] = next.getValue();
                    }
                    fieldIterator.remove();
                } else if (InternalUtils.isSubPath(fieldPath, field)) {
                    throw new IllegalArgumentException("operate conflict on field:" + fieldPath);
                }
            }
        });
        return ret[0];
    }

    private boolean hasOperate(String field) {
        boolean[] ret = new boolean[]{false};
        traverseAllOperate((s, bsonDocument) -> {
            if (ret[0]) {
                return;
            }
            Iterator<Map.Entry<String, BsonValue>> fieldIterator = bsonDocument.entrySet().iterator();
            while (fieldIterator.hasNext()) {
                Map.Entry<String, BsonValue> next = fieldIterator.next();
                String fieldPath = next.getKey();
                if (InternalUtils.isEqOrSubPath(field, fieldPath)) {
                    ret[0] = true;
                    break;
                }
            }
        });
        return ret[0];
    }

    /**
     * 遍历所有的操作符
     *
     * @param consumer <操作符，操作符内容>
     */
    private void traverseAllOperate(BiConsumer<String, BsonDocument> consumer) {
        convertToBsonDocument();
        Iterator<Map.Entry<String, BsonValue>> opIterator = this.bsonDocument.entrySet().iterator();
        while (opIterator.hasNext()) {
            Map.Entry<String, BsonValue> op = opIterator.next();
            if (ALL_OPERATORS.contains(op.getKey()) && op.getValue().isDocument()) {
                BsonDocument bsonDocument = op.getValue().asDocument();
                consumer.accept(op.getKey(), bsonDocument);
                if (bsonDocument.isEmpty()) {
                    opIterator.remove();
                }
            }
        }
    }

}
