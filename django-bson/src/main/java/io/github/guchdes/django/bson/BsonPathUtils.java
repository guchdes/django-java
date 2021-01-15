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

import io.github.guchdes.django.bson.projection.dtbson.DtBsonDocument;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class BsonPathUtils {

    /**
     * 获取多级路径上的元素，如果路径不存在则返回null。
     *
     * @param bsonDocument bsonDocument
     * @param path         路径，多层字段用.隔开
     */
    public static BsonValue getPathElement(BsonValue bsonDocument, String path) {
        String[] strings = path.split("\\.", 2);
        String curPath = strings[0];
        if (bsonDocument instanceof BsonArray) {
            BsonArray bsonArray = bsonDocument.asArray();
            try {
                int parseInt = Integer.parseInt(curPath);
                if (parseInt >= bsonArray.size()) {
                    return null;
                }
                BsonValue bsonValue = bsonArray.get(parseInt);
                if (bsonValue != null && strings.length > 1 && !strings[1].isEmpty()) {
                    return getPathElement(bsonValue, strings[1]);
                } else {
                    return bsonValue;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("BsonArray path not number" + bsonDocument + ", path " + curPath);
            }
        } else if (bsonDocument instanceof BsonDocument) {
            BsonValue bsonValue = ((BsonDocument) bsonDocument).get(curPath);
            if (bsonValue != null && strings.length > 1 && !strings[1].isEmpty()) {
                return getPathElement(bsonValue, strings[1]);
            } else {
                return bsonValue;
            }
        } else {
            throw new IllegalArgumentException("Not BsonArray or BsonDocument:" + bsonDocument);
        }
    }

    /**
     * 移除多级路径上的元素，不能移除数组中的元素
     *
     * @param bsonDocument bsonDocument
     * @param path         路径，多层字段用.隔开
     * @return 是否移除
     */
    public static boolean removePathElement(BsonValue bsonDocument, String path) {
        String[] strings = splitByFirstDot(path);
        String curPath = strings[0];
        if (bsonDocument instanceof BsonDocument) {
            BsonDocument document = (BsonDocument) bsonDocument;
            BsonValue bsonValue = document.get(curPath);
            if (bsonValue == null) {
                return false;
            }
            if (strings.length > 1 && !strings[1].isEmpty()) {
                return removePathElement(bsonValue, strings[1]);
            } else {
                document.remove(curPath);
                return true;
            }
        } else if (bsonDocument instanceof BsonArray) {
            throw new IllegalArgumentException("Can not remove from array");
        } else {
            throw new IllegalArgumentException("Not BsonArray or BsonDocument:" + bsonDocument);
        }
    }

    /**
     * 等于 s.split("\\.", 2)
     */
    private static String[] splitByFirstDot(String s) {
        int index = s.indexOf('.');
        if (index == -1) {
            return new String[]{s};
        } else {
            return new String[]{s.substring(0, index), s.substring(index)};
        }
    }

}
