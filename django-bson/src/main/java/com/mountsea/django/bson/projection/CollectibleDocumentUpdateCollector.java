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
package com.mountsea.django.bson.projection;

import lombok.Getter;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * 当修改了CollectibleDocument的key字段之后，CollectibleDocument和原数据库文档失去关联，此时 disableUpdateCollect
 *
 * @author guch
 * @since 3.0.0
 */
public class CollectibleDocumentUpdateCollector extends SyncMongoUpdateCollector {

    /**
     * 只有字段本身占用空间，Set对象应该是一个类共用一个
     */
    @Getter
    private final Set<String> keyFieldNames;

    public CollectibleDocumentUpdateCollector(Set<String> keyFieldNames) {
        this.keyFieldNames = keyFieldNames;
    }

    @Override
    protected void recordUpdate(Op op, String path, Object fieldValue, Object previousValue,
                                boolean collectionBatchOp, Object collectionOpValue, Collection<?> collectionBatchOpValues) {
        String field = getTopField(path);
        if (!keyFieldNames.contains(field) || (op.isAssign() && Objects.equals(fieldValue, previousValue))) {
            super.recordUpdate(op, path, fieldValue, previousValue, collectionBatchOp, collectionOpValue, collectionBatchOpValues);
        } else {
            disableUpdateCollect();
        }
    }

    private String getTopField(String path) {
        int index = path.indexOf('.');
        if (index < 0) {
            return path;
        } else {
            return path.substring(0, index);
        }
    }
}
