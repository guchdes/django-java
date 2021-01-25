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
package com.mountsea.django.core;

import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.mountsea.django.core.model.BulkSaveResult;
import com.mountsea.django.core.model.MultiEquals;
import com.mountsea.django.core.model.SaveMode;
import com.mountsea.django.core.model.SaveResult;
import org.bson.conversions.Bson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author guch
 * @since 3.0.0
 */
public interface CollectionOperationalDao {

    /* 查找 */

    @Nonnull
    <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters);

    @Nonnull
    <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters, @Nullable Bson projection);

    @Nonnull
    <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters, @Nullable Bson projection,
                                                    int skip, int limit);

    @Nonnull
    <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters, @Nullable Bson projection,
                                                    Bson sort);

    @Nonnull
    <T extends CollectibleDocument> List<T> findAll(Class<T> documentClass, Bson filters, @Nullable Bson projection,
                                                    int skip, int limit, Bson sort);

    /**
     * 根据条件查找。
     * <p>
     * 如果是一个或多个字段等于某个值的条件，可以用 {@link MultiEquals}表示条件.
     */
    @Nullable
    <T extends CollectibleDocument> T findOne(Class<T> documentClass, Bson filters);

    /**
     * 根据条件查找。
     * <p>
     * 如果是一个或多个字段等于某个值的条件，可以用 {@link MultiEquals}表示条件.
     */
    @Nullable
    <T extends CollectibleDocument> T findOne(Class<T> documentClass, Bson filters, @Nullable Bson projection);

    /**
     * 根据一组字段查找，不存在则初始化插入，初始化插入的文档用 {@link MultiEquals} 中的条件字段做为初始值
     * 如果 MultiEquals中不包含id，则类的id类型必须是ObjectId。如果查询时由于文档不存在进行初始化插入，则mongo server自动生成id。
     */
    @Nonnull
    <T extends CollectibleDocument> T getByFields(Class<T> documentClass, MultiEquals multiEquals);

    /**
     * 根据一组字段查找，不存在则初始化插入，初始化插入的文档用 {@link MultiEquals} 中的条件字段做为初始值
     * 如果 MultiEquals中不包含id，则类的id类型必须是ObjectId。如果查询时由于文档不存在进行初始化插入，则mongo server自动生成id。
     */
    @Nonnull
    <T extends CollectibleDocument> T getByFields(Class<T> documentClass, MultiEquals multiEquals, @Nullable Bson projection);

    /* 删除 */

    /**
     * 根据条件删除单个文档。
     * <p>
     * 如果是一个或多个字段等于某个值的条件，可以用 {@link MultiEquals}表示条件.
     */
    boolean deleteOne(Class<? extends CollectibleDocument> documentClass, Bson filters, @Nullable DeleteOptions deleteOptions);

    /**
     * 根据条件删除多个文档
     *
     * @return 返回删除的文档个数
     */
    long deleteMany(Class<? extends CollectibleDocument> documentClass, Bson filters, @Nullable DeleteOptions deleteOptions);

    /* 更新 */

    /**
     * 根据条件更新单个文档
     *
     * @param filter 条件
     * @param update 更新内容
     * @return 返回是否有更新
     */
    boolean updateOne(Class<? extends CollectibleDocument> documentClass, Bson filter, Bson update,
                      @Nullable UpdateOptions updateOptions);

    /**
     * 根据条件更新多个文档
     *
     * @param filter 条件
     * @param update 更新内容
     * @return 返回是否有更新
     */
    UpdateResult updateMany(Class<? extends CollectibleDocument> documentClass, Bson filter, Bson update,
                            @Nullable UpdateOptions updateOptions);

    /**
     * 保存一个文档。
     * 当SaveMode为 INSERT_OR_UPDATE或UPDATE_ONLY时，用fields参数指定的字段做为filter（字段值从document中取）。
     * 当SaveMode为 INSERT_ONLY时，不需要fields参数。
     * <p>
     * 如果 fields中不包含id，则使用INSERT_ONLY/INSERT_OR_UPDATE时需要自动生成id，此时id的类型必须是ObjectId才能使用此方法。如果自动生成了id，
     * 在save完成后自动设置id到文档。
     * <p>
     * 文档更新内容和并发安全方面参考 {@link KeyOperationalDao#saveByKey(CollectibleDocument, SaveMode)}
     *
     * @param fields 做为filter的字段，id字段名为id，如果包含id字段则不能有其他字段
     */
    SaveResult saveByFields(CollectibleDocument document, SaveMode saveMode, List<String> fields);

    /**
     * 批量保存
     *
     * @see #saveByFields(CollectibleDocument, SaveMode, List)
     */
    BulkSaveResult bulkSaveByFields(List<? extends CollectibleDocument> documents, SaveMode saveMode, List<String> fields);

}
