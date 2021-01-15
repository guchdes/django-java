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

import io.github.guchdes.django.core.DatabaseDao;
import io.github.guchdes.django.core.KeyOperationalDao;

/**
 * @Author guch
 * @Since 3.0.0
 * @see KeyOperationalDao#saveByKey
 * @see DatabaseDao#saveByFields
 */
public enum SaveMode {
    /**
     * 插入或更新文档，如果库中已存在指定的filter (多个'字段值相等'条件)，则是更新，否则是插入
     */
    INSERT_OR_UPDATE,
    /**
     * 只插入文档，如果库中已存在指定的filter，则抛出异常 {@link com.mongodb.DuplicateKeyException}
     */
    INSERT_ONLY,
    /**
     * 只更新文档，如果库中不存在指定的filter，则不执行任何操作
     */
    UPDATE_ONLY
}
