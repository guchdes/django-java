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

import io.github.guchdes.django.core.annotation.VersionField;
import io.github.guchdes.django.core.exception.ConcurrentSaveDjangoException;
import io.github.guchdes.django.core.exception.DocumentNoIdDjangoException;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author guch
 * @since 3.0.0
 */
public interface ConcurrentOperationalDao {

    /**
     * 根据version字段对文档cas更新。如果库中的version字段和参数文档中的一样，则更新成功，否则更新失败。
     * 更新成功后，version自动+1，包括库中和参数文档。
     * <p>
     * 此方法主要用于实现分布式环境下的业务逻辑，通常是[查询文档-判断-修改-入库文档]。
     * 多线程对同一个文档对象调用此方法是并发安全的，但执行过程会加文档的saveLock锁。
     *
     * @param document 带version的文档， {@link VersionField}
     * @return 是否版本号匹配，匹配就是更新成功。如果库中没有此id的文档，则更新失败。
     * @throws DocumentNoIdDjangoException 如果文档没有id
     */
    boolean casUpdateByVersion(CollectibleDocument document) throws DocumentNoIdDjangoException;

    boolean casUpdateByVersion(CollectibleDocument document, String versionField) throws DocumentNoIdDjangoException;

    /**
     * casUpdateByVersion的更新多个文档的版本。
     * 在事务中更新多个文档(多个文档可以是不同的类)，如果多个文档的version字段都和库中文档匹配，则更新成功，否则全部更新失败。
     * 更新成功后，参数中所有文档version自动+1。
     * <p>
     * 此方法主要用于实现分布式环境下的业务逻辑，通常是[查询文档-判断-修改-入库文档]，此类文档是临时对象，不用缓存也不会跨线程共享。
     * 不要多线程对相同的文档对象调用此方法，否则可能抛出异常 {@link ConcurrentSaveDjangoException}。
     *
     * @param documentList 带version的文档
     * @return 是否所有文档都版本号匹配，匹配就是更新成功。如果库中没有此id的文档，则更新失败。
     * @throws DocumentNoIdDjangoException 如果文档没有id
     * @see #casUpdateByVersion
     */
    boolean casUpdateMultiByVersion(List<CollectibleDocument> documentList) throws DocumentNoIdDjangoException;

    /**
     * 可以指定每个文档的version字段名
     *
     * @see #casUpdateMultiByVersion(List)
     */
    boolean casUpdateMultiByVersionNames(List<DocumentAndVersionField> documentList) throws DocumentNoIdDjangoException;

    @Data
    @AllArgsConstructor
    class DocumentAndVersionField {
        private final CollectibleDocument document;
        private final String versionField;
    }

    /**
     * 增加一个字段的值。原子操作。
     * 如果没有此id的文档，则初始化插入再执行。
     *
     * @return 自增后的值
     */
    <T extends CollectibleDocument> long atomicIncAndGetField(Class<T> documentClass, Object id, String fieldName, long inc);

    interface AtomicDocumentTransformer<T> {
        /**
         * 操作文档，返回要入库的文档，如果放弃入库，则返回null
         */
        @Nullable
        T accept(T document);
    }

    /**
     * 对文档做原子的转换操作。读取一个文档，根据当前值得出更新值，然后入库更新值，如果入库时刚读取的值已被改变，则重新执行此过程,
     * 直到成功或者transformer返回null。
     * 可以实现类似于 {@link AtomicInteger#incrementAndGet()}的操作，不过要做自增用另一个方法 {@link #atomicIncAndGetField}，
     * 性能更好。
     * <p>
     * 如果没有此id的文档，则初始化插入再执行。
     *
     * @param firstDocument 初始值，如果为空，则第一次就从数据库读取
     * @param fieldNames    要读取和对比的字段，入库时对比这些字段是否有变化，有变化则重来
     * @param transformer   转换文档
     * @param <T>
     * @return 更新成功后返回转换后的文档，如果转换取消 {@link AtomicDocumentTransformer#accept 返回null}则返回null
     */
    <T extends CollectibleDocument> T atomicTransformDocument(Class<T> documentClass, Object id, @Nullable T firstDocument,
                                                              List<String> fieldNames,
                                                              AtomicDocumentTransformer<T> transformer);

}
