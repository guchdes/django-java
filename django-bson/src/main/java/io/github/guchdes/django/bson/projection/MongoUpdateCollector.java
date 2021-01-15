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
package io.github.guchdes.django.bson.projection;

import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对象更新监听/记录接口
 *
 * @Author guch
 * @Since 3.0.0
 */
public interface MongoUpdateCollector extends BasicUpdateCollector<Bson> {

    /**
     * 更新记录结构类。
     * <p>
     * 更新记录的规则：
     * 所有可用的操作符有 $set,$unset,$push(each),$pull(All),$addToSet(each)，其中$set/$unset是字段赋值操作，其他是数组操作。
     * <p>
     * 新旧记录路径相同时，新的字段赋值操作覆盖旧的任何操作，新的数组操作仅当和原有操作符相同时合并为批量操作，
     * 否则数组操作变为对容器字段本身的$set操作.
     * <p>
     * 新旧记录路径不同但有重合时，保留路径更短的一个。同时如果保留的记录的操作符不是赋值，则改为赋值。
     */
    interface MongoUpdate {
        Map<String, Object> getSet();

        Set<String> getUnset();

        Map<String, List<Object>> getPush();

        Map<String, List<Object>> getPull();

        Map<String, List<Object>> getAddToSet();
    }

    /**
     * 获取MongoUpdate格式的更新记录
     */
    @Nullable
    MongoUpdate getMongoUpdate();

    /**
     * $set 操作
     */
    void setField(String path, Object value, Object previousValue);

    /**
     * $unset 操作
     */
    void unsetField(String path, Object previousValue);

    /**
     * $push 操作
     *
     * @param path       容器路径
     * @param collection 容器对象
     * @param value      操作元素
     */
    void pushArrayValue(String path, Collection<?> collection, Object value);

    /**
     * $push 操作. 批量添加
     *
     * @param path       容器路径
     * @param collection 容器对象
     * @param values     操作元素
     */
    void pushArrayValueBatch(String path, Collection<?> collection, Collection<?> values);

    /**
     * $pull 操作
     *
     * @param path       容器路径
     * @param collection 容器对象
     * @param value      操作元素
     */
    void pullArrayValue(String path, Collection<?> collection, Object value);

    /**
     * $pull 操作. 批量添加
     *
     * @param path       容器路径
     * @param collection 容器对象
     * @param values     操作元素
     */
    void pullArrayValueBatch(String path, Collection<?> collection, Collection<?> values);

    /**
     * $addToSet 操作
     *
     * @param path       容器路径
     * @param collection 容器对象
     * @param value      操作元素
     */
    void addToSetArrayValue(String path, Collection<?> collection, Object value);

    /**
     * $addToSet 操作. 批量添加
     *
     * @param path       容器路径
     * @param collection 容器对象
     * @param values     操作元素
     */
    void addToSetArrayValueBatch(String path, Collection<?> collection, Collection<?> values);

}
