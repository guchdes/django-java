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

import com.mongodb.client.model.Updates;
import com.mountsea.django.bson.util.InternalUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guch
 * @since 3.0.0
 */
public class MongoUpdateCollectorImpl implements MongoUpdateCollector {

    /**
     * 自动生成的操作类型.
     * <p>
     * 相同路径时:
     * set/unset相互覆盖
     * set/unset覆盖push/pull且不能被反覆盖
     * 两个push相互叠加，变为pushEach
     * push与pull叠加，变成set
     */
    public enum Op {
        $set,
        $unset, // 生成的bson中, unset的字段名对应value为空字符串
        $push,
        $pull,
        $addToSet,
        ;

        public boolean isAssign() {
            return this == $set || this == $unset;
        }
    }

    static class UpdateRecord {
        private Op op;
        private String path;

        /**
         * set时是赋值对象, unset时是null, pull和push时是容器对象
         */
        private Object fieldValue;

        /**
         * pull和push时是被操作的元素
         */
        private List<Object> elementValues;

        Bson toBson() {
            Bson bson = null;
            switch (op) {
                case $set:
                    bson = Updates.set(path, fieldValue);
                    break;
                case $unset:
                    bson = Updates.unset(path);
                    break;
                case $push:
                    if (elementValues.size() == 1) {
                        bson = Updates.push(path, elementValues.get(0));
                    } else {
                        bson = Updates.pushEach(path, elementValues);
                    }
                    break;
                case $pull:
                    if (elementValues.size() == 1) {
                        bson = Updates.pull(path, elementValues.get(0));
                    } else {
                        bson = Updates.pullAll(path, elementValues);
                    }
                    break;
                case $addToSet:
                    if (elementValues.size() == 1) {
                        bson = Updates.addToSet(path, elementValues.get(0));
                    } else {
                        bson = Updates.addEachToSet(path, elementValues);
                    }
                    break;
            }
            return bson;
        }

        public Op getOp() {
            return op;
        }

        public void setOp(Op op) {
            this.op = op;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Object getFieldValue() {
            return fieldValue;
        }

        public void setFieldValue(Object fieldValue) {
            this.fieldValue = fieldValue;
        }

        public List<Object> getElementValues() {
            return elementValues;
        }

        public void setElementValues(List<Object> elementValues) {
            this.elementValues = elementValues;
        }

        public UpdateRecord(Op op, String path, Object fieldValue) {
            this.op = op;
            this.path = path;
            this.fieldValue = fieldValue;
        }

    }

    List<UpdateRecord> list;

    private List<UpdateRecord> getList() {
        if (list == null) {
            list = new LinkedList<>();
        }
        return list;
    }

    /**
     * 合并路径冲突的操作
     *
     * @param shortPath    合并后被保留
     * @param longPath     合并后丢弃
     * @param isShortFirst 是否shortPath上的操作先被添加
     */
    private void onMergeOp(UpdateRecord shortPath, UpdateRecord longPath, boolean isShortFirst) {
        if (shortPath.getPath().length() == longPath.getPath().length() &&
                shortPath.getOp() == longPath.getOp() && !shortPath.getOp().isAssign()) {
            // 是相同的数组操作, 且路径一样, 合并为each..
            if (isShortFirst) {
                shortPath.getElementValues().addAll(longPath.getElementValues());
            } else {
                longPath.getElementValues().addAll(shortPath.getElementValues());
                shortPath.setElementValues(longPath.getElementValues()); // 替换List
            }
        } else {
            // 其他情况一律合并为set操作
            if (!shortPath.op.isAssign()) {
                shortPath.op = Op.$set;
            }
        }
    }


    public void add(UpdateRecord updateRecord) {
        if (list == null) {
            getList().add(updateRecord);
            return;
        }

        Iterator<UpdateRecord> iterator = list.iterator();
        boolean isAdd = true;
        while (iterator.hasNext()) {
            UpdateRecord next = iterator.next();
            if (InternalUtils.isEqOrSubPath(updateRecord.getPath(), next.path)) {
                iterator.remove();
                onMergeOp(updateRecord, next, false);
                continue;
            }
            if (InternalUtils.isSubPath(next.path, updateRecord.getPath())) {
                isAdd = false;
                onMergeOp(next, updateRecord, true);
                // 不会再存在路径重叠
                break;
            }
        }
        if (isAdd) {
            list.add(updateRecord);
        }
    }

    @Nullable
    @Override
    public Bson getUpdate() {
        if (list == null) {
            return null;
        }
        return Updates.combine(list.stream()
                .map(UpdateRecord::toBson).collect(Collectors.toList()));
    }

    @Nullable
    @Override
    public Bson getUpdate(Set<String> excludePaths) {
        if (list == null) {
            return null;
        }
        return Updates.combine(list.stream()
                .filter(x -> !hasExclude(excludePaths, x.path))
                .map(UpdateRecord::toBson).collect(Collectors.toList()));
    }

    @Override
    @Nullable
    public MongoUpdate getMongoUpdate() {
        if (list == null) {
            return null;
        }

        Map<String, Object> set = null;
        Set<String> unset = null;
        Map<String, List<Object>> push = null;
        Map<String, List<Object>> pull = null;
        Map<String, List<Object>> addToSet = null;
        for (UpdateRecord updateRecord : list) {
            switch (updateRecord.op) {
                case $set:
                    (set == null ? set = new HashMap<>() : set).put(updateRecord.path, updateRecord.fieldValue);
                    break;
                case $unset:
                    (unset == null ? unset = new HashSet<>() : unset).add(updateRecord.path);
                    break;
                case $push:
                    (push == null ? push = new HashMap<>() : push).put(updateRecord.path, updateRecord.elementValues);
                    break;
                case $pull:
                    (pull == null ? pull = new HashMap<>() : pull).put(updateRecord.path, updateRecord.elementValues);
                    break;
                case $addToSet:
                    (addToSet == null ? addToSet = new HashMap<>() : addToSet).put(updateRecord.path, updateRecord.elementValues);
                    break;
            }
        }
        return new MongoUpdateImpl(set == null ? Collections.emptyMap() : set,
                unset == null ? Collections.emptySet() : unset,
                push == null ? Collections.emptyMap() : push,
                pull == null ? Collections.emptyMap() : pull,
                addToSet == null ? Collections.emptyMap() : addToSet);
    }

    private boolean hasExclude(Set<String> excludePaths, String path) {
        for (String excludePath : excludePaths) {
            if (InternalUtils.isEqOrSubPath(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearUpdate() {
        list = null;
    }

    protected void recordUpdate(Op op, String path, Object fieldValue, Object previousValue,
                                boolean collectionBatchOp, Object collectionOpValue, Collection<?> collectionBatchOpValues) {
        if (!op.isAssign() && fieldValue == null) throw new IllegalArgumentException();
        if (collectionBatchOp && collectionBatchOpValues == null) throw new IllegalArgumentException();

        UpdateRecord updateRecord = new UpdateRecord(op, path, fieldValue);
        if (!op.isAssign()) {
            if (collectionBatchOp) {
                updateRecord.setElementValues(new ArrayList<>(collectionBatchOpValues));
            } else {
                ArrayList<Object> list = new ArrayList<>(1);
                list.add(collectionOpValue);
                updateRecord.setElementValues(list);
            }
        }
        add(updateRecord);
    }

    @Override
    public void setField(String path, Object value, Object previousValue) {
        recordUpdate(Op.$set, path, value, previousValue,
                false, null, null);
    }

    @Override
    public void unsetField(String path, Object previousValue) {
        recordUpdate(Op.$unset, path, null, previousValue,
                false, null, null);
    }

    @Override
    public void pushArrayValue(String path, Collection<?> collection, Object value) {
        recordUpdate(Op.$push, path, collection, collection,
                false, value, null);
    }

    @Override
    public void pushArrayValueBatch(String path, Collection<?> collection, Collection<?> values) {
        recordUpdate(Op.$push, path, collection, collection,
                true, null, values);
    }

    @Override
    public void pullArrayValue(String path, Collection<?> collection, Object value) {
        recordUpdate(Op.$pull, path, collection, collection,
                false, value, null);
    }

    @Override
    public void pullArrayValueBatch(String path, Collection<?> collection, Collection<?> values) {
        recordUpdate(Op.$pull, path, collection, collection,
                true, null, values);
    }

    @Override
    public void addToSetArrayValue(String path, Collection<?> collection, Object value) {
        recordUpdate(Op.$addToSet, path, collection, collection,
                false, value, null);
    }

    @Override
    public void addToSetArrayValueBatch(String path, Collection<?> collection, Collection<?> values) {
        recordUpdate(Op.$addToSet, path, collection, collection,
                true, null, values);
    }

    @Getter
    @ToString
    @AllArgsConstructor
    private static class MongoUpdateImpl implements MongoUpdate {

        private final Map<String, Object> set;

        private final Set<String> unset;

        private final Map<String, List<Object>> push;

        private final Map<String, List<Object>> pull;

        private final Map<String, List<Object>> addToSet;
    }
}
