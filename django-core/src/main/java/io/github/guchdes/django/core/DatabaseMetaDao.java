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

import io.github.guchdes.django.core.annotation.CollectionName;

/**
 * Dao对象绑定MongoDatabase，操作时在此Database内操作（Database是命名空间，一个应用通常使用一个Database）
 * <p>
 * 一个 {@link CollectibleDocument} 文档类对应一个MongoCollection，应用不需要管理文档存在哪个表，只需要定义文档类名和结构。
 * 默认类名对应collection，可以用{@link CollectionName}注解指定collection。
 *
 * @Author guch
 * @Since 3.0.0
 */
public interface DatabaseMetaDao {

    /**
     * @return DatabaseName 返回当前Dao关联的DatabaseName，不可改变
     */
    String getDatabaseName();

    /**
     * @return 创建此对象的Factory
     */
    DatabaseDaoFactory getDatabaseDaoFactory();

}
