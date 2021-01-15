/**
 * MIT License
 *
 * Copyright (c) 2021 fengniao studio
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

import io.github.guchdes.django.bson.projection.*;
import io.github.guchdes.django.core.annotation.KeyClass;
import io.github.guchdes.django.core.annotation.KeyField;
import io.github.guchdes.django.core.annotation.KeyFieldName;
import org.bson.conversions.Bson;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link CollectibleDocument} 表示数据层的文档对象，每个CollectibleDocument的子类(abstract除外)对应一个mongodb的collection。
 * CollectibleDocument的结构用来表示collection的结构，同时属于DocumentNode，有记录修改的功能，但在字段类型和使用方式上有限制，
 * 可见 {@link DocumentNode}
 * <p>
 * CollectibleDocument 可以记录文档中的修改，并在保存文档时自动生成更新语句。
 * 对文档的修改操作和生成的更新语句对应关系为：
 * - 字段赋值、map.put对应$set
 * - 字段赋值为null、map.put(null)、map.remove对应$unset
 * - collection.add对应$push、collection.remove时更新整个数组(对应$set)
 * - set.add对应$addToSet、set.remove对应$pull
 * - collection.clear、List排序、List中间插入元素等等对应更新整个数组
 * <p>
 * 每个CollectibleDocument有且只有一个Key，对应一个unique索引，使用 {@link KeyOperationalDao}中的方法可以根据Key进行操作。
 * Key可以是文档的一个字段 (不能是子文档的字段)或多个字段组合成的结构体类。
 * 在文档类中用{@link KeyField}或{@link KeyClass}或{@link KeyFieldName}注解定义文档的key。
 * 如果一个类没有注解key，则从其父类查找注解key，如果还是没有，则默认id为key。
 *
 * @Author guch
 * @Since 3.0.0
 */
public abstract class CollectibleDocument extends DocumentNode implements CollectibleDocumentNode {

    /**
     * 保存文档时，对[获取文档更新内容+入库]过程加的锁，用来保证多线程修改并保存同一文档时，文档的最终状态和数据库中一致。
     * saveLock锁定时文档仍然可以修改。
     */
    private final Lock __saveLock = new ReentrantLock();

    public abstract Object getId();

    /**
     * 用 {@link DatabaseDao#getByKey}/{@link DatabaseDao#getByFields} 方法获取文档时，如果库中文档不存在，
     * 将创建空文档, 并使用此方法填充文档, 然后入库.
     */
    public void initForStore() {
    }

    @Override
    protected final SyncMongoUpdateCollector createUpdateCollector() {
        CollectibleDocumentDefinition definition = CollectibleDocumentDefinitions.getDocumentDefinition(this.getClass());
        Set<String> keyNames = definition.getKeyDefinition().getPropertyMap().keySet();
        return new CollectibleDocumentUpdateCollector(keyNames);
    }

    Lock getSaveLock() {
        return __saveLock;
    }
}
