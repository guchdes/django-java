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

import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * insert和update的内容文档，如果是insert则保存目标对象，如果是update则保存操作符加操作内容。
 * <p>
 * 可以做简单的字段增删，增删时要处理操作符中的内容
 *
 * @author guch
 * @since 3.0.0
 */
interface WriteContentDocument {
    String SET_OPERATOR = "$set";

    Set<String> ALL_OPERATORS = new HashSet<>(Arrays.asList(
            "$currentDate", "$inc", "$min", "$max", "$mul", "$rename", "$set", "$setOnInsert", "$unset"));

    enum Mode {
        INSERT, // 保存原文档对象
        UPDATE // 保存操作符+内容
    }

    Mode getMode();

    Bson getAsBson();

    /**
     * @see WriteContentDocumentCodec
     */
    Object getEncodingObject();

    /**
     * 新增字段，如果是update状态，则添加到$set操作符中
     */
    void put(String field, Object value);

    Object remove(String field);

    boolean contains(String field);

    boolean isEmpty();
}
