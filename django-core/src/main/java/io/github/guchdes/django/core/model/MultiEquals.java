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
package io.github.guchdes.django.core.model;

import com.mongodb.client.model.Filters;
import io.github.guchdes.django.bson.BsonUtils;
import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 一个或多个 [字段等于某个值] 的条件.
 * <p>
 * 示例：
 * MultiEquals.start("name", "mike").and("age", 20)
 *
 * @Author guch
 * @Since 3.0.0
 */
public class MultiEquals implements Bson {

    private static final String ID = "_id";

    Map<String, Object> map = new HashMap<>();

    public static MultiEquals with(String name, Object v) {
        MultiEquals builder = new MultiEquals();
        return builder.and(name, v);
    }

    public static MultiEquals withId(Object v) {
        return with(ID, v);
    }

    public MultiEquals and(String name, Object v) {
        Object pre = map.putIfAbsent(name, v);
        if (pre != null) {
            throw new IllegalStateException("already has field:" + name);
        }
        return this;
    }

    public MultiEquals andId(Object v) {
        return and(ID, v);
    }

    public Bson toBson() {
        if (map.isEmpty()) {
            throw new IllegalStateException();
        }
        if (map.size() > 1) {
            return Filters.and(map.entrySet().stream()
                    .map(x -> Filters.eq(x.getKey(), x.getValue())).collect(Collectors.toList()));
        } else {
            Map.Entry<String, Object> entry = map.entrySet().iterator().next();
            return Filters.eq(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Object> getAllFields() {
        return map;
    }

    @Override
    public <TDocument> BsonDocument toBsonDocument(Class<TDocument> tDocumentClass, CodecRegistry codecRegistry) {
        return toBson().toBsonDocument(tDocumentClass, codecRegistry);
    }

    public BsonDocument toBsonDocument() {
        return BsonUtils.toBsonDocument(map);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MultiEquals f = (MultiEquals) o;

        return map.equals(f.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return "MultiEquals{"
                + "fields=" + map
                + '}';
    }
}
