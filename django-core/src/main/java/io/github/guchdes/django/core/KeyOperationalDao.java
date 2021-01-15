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

import io.github.guchdes.django.core.annotation.AllowNullKeyField;
import io.github.guchdes.django.core.exception.ConcurrentSaveDjangoException;
import io.github.guchdes.django.core.model.BulkSaveResult;
import io.github.guchdes.django.core.model.SaveMode;
import io.github.guchdes.django.core.model.SaveResult;
import org.bson.conversions.Bson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * KeyDocumentDao提供根据文档的Key进行CRUD操作的方法。
 * <p>
 * <h3>文档的Key</>
 * key对应unique索引，每个文档类有且只有一个Key，Key可以包含一个或多个字段，默认是一个id字段。
 * 通常文档只有一个key可以满足大部分需求，如果需要根据key以外的filter进行操作，可以使用 {@link DatabaseDao}中的使用Bson filter的方法，
 * 或者使用原生驱动实现。
 * mongodb中表示字段为空有字段不存在和字段值为Null两种情况，在Key中使用null字段值时，这两种情况都可以匹配。供。
 * 在创建Dao对象时还可以为每个Dao对象独立配置是否使用缓存，通过 {@link #isCacheEnable()}确认当前Dao是否使用缓存。
 *
 * @Author guch
 * @Since 3.0.0
 * @see CollectibleDocument
 */
public interface KeyOperationalDao {

    /* 查找 */

    /**
     * 根据key查找
     */
    @Nullable
    @CacheAware
    <T extends CollectibleDocument> T findByKey(Class<T> documentClass, Object key);

    /**
     * 根据key查找，并选择返回的字段。
     * <p>
     * 此方法不使用缓存。
     */
    @Nullable
    <T extends CollectibleDocument> T findByKey(Class<T> documentClass, Object key, Bson projection);

    /**
     * 根据key获取，如果不存在则按照key字段初始化并插入。
     * 如果key不是id，则类的id必须是ObjectId类型才能使用此方法。如果查询时由于文档不存在进行初始化插入，则mongo server自动生成id。
     */
    @Nonnull
    @CacheAware
    <T extends CollectibleDocument> T getByKey(Class<T> documentClass, Object key);

    /**
     * 根据key获取,如果不存在则按照key字段初始化并插入。
     * 如果key不是id，则类的id必须是ObjectId类型才能使用此方法。如果查询时由于文档不存在进行初始化插入，则mongo server自动生成id。
     * <p>
     * 此方法不使用缓存。
     *
     * @param projection 选择字段，如果为null则返回全部字段
     */
    @Nonnull
    <T extends CollectibleDocument> T getByKey(Class<T> documentClass, Object key, @Nullable Bson projection);

    /* save操作 */

    /**
     * 等于用 {@link SaveMode#INSERT_OR_UPDATE}调用{@link #saveByKey(CollectibleDocument, SaveMode)}
     */
    @CacheAware
    SaveResult saveByKey(CollectibleDocument document);

    /**
     * 根据key保存入库，当SaveMode为 {@link SaveMode#INSERT_OR_UPDATE}或{@link SaveMode#UPDATE_ONLY}时，用文档的key做为filter。
     * 如果SaveMode为INSERT_ONLY则插入整个文档。
     * <p>
     * key字段中不能有null值，除非文档类有 {@link AllowNullKeyField}注解。
     * 如果key不是id，则使用INSERT_ONLY/INSERT_OR_UPDATE时需要自动生成id，此时id的类型必须是ObjectId才能使用此方法。如果自动生成了id，
     * 在save完成后自动设置id到文档。
     * <p>
     * 如果是更新，更新内容从文档获取，文档中会记录上次save之后的修改。
     * 在多线程下对同一个文档做修改并调用saveByKey是并发安全的，[获取文档更新内容]+[更新入库]是在加了文档内部的锁后执行，
     * 内存中文档的最终状态会和库中保持一致。
     */
    @CacheAware
    SaveResult saveByKey(CollectibleDocument document, SaveMode saveMode);

    /**
     * 使用 SaveMode.INSERT_OR_UPDATE调用 {@link #bulkSaveByKey(List, SaveMode)}
     */
    @CacheAware
    BulkSaveResult bulkSaveByKey(List<? extends CollectibleDocument> list);

    /**
     * 批量保存。list中只能是同一个类的文档，因为批量操作只能是对同一个表的操作。
     * 功能上和 {@link #saveByKey}一样，实现上是通过 bulkWrite 批量写入。
     * <p>
     * 不能在多线程环境下对相同的文档调用此方法，否则将会(有几率)抛出 {@link ConcurrentSaveDjangoException} 异常 。
     */
    @CacheAware
    BulkSaveResult bulkSaveByKey(List<? extends CollectibleDocument> list, SaveMode saveMode);

    /* 删除操作 */

    /**
     * 根据key删除，document中的key字段不能为null
     */
    @CacheAware
    boolean deleteByKey(CollectibleDocument document);

    /**
     * 根据key删除，key字段不能为null
     */
    @CacheAware
    boolean deleteByKey(Class<? extends CollectibleDocument> documentClass, Object key);

    /**
     * 批量删除。
     * list中只能是同一个类的文档，因为批量操作只能是对同一个表的操作。
     */
    @CacheAware
    BulkSaveResult bulkDeleteByKey(List<? extends CollectibleDocument> document);

    /**
     * 批量删除
     */
    @CacheAware
    BulkSaveResult bulkDeleteByKey(Class<? extends CollectibleDocument> documentClass, List<Object> key);

    /**
     * 是否允许使用缓存
     */
    boolean isCacheEnable();

}