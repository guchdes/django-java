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
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class MongoModels {

    public static final String _id = "_id";

    public static Bson idEquals(Object id) {
        return Filters.eq(_id, id);
    }

    /**
     * 获取projection bson。 如果参数为空则返回null。
     * (projection为null表示获取所有字段)
     */
    @Nullable
    public static Bson projectionByFields(String... fields) {
        return projectionByFields(Arrays.asList(fields));
    }

    @Nullable
    public static Bson projectionByFields(List<String> fields) {
        if (fields.isEmpty()) {
            return null;
        }
        Document document = new Document();
        for (String field : fields) {
            document.put(field, 1);
        }
        return document;
    }


}
