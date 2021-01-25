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

import lombok.Getter;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;

/**
 * @author guch
 * @since 3.0.0
 */
@Getter
class InternalSaveDocument {

    private final CollectibleDocument document;

    private final Bson filter;

    private final WriteContentDocument writeContent;

    /**
     * 缓存key，如果为空，则不使用缓存
     */
    @Nullable
    private final Object cacheKey;

    public InternalSaveDocument(CollectibleDocument document, Bson filter,
                                @Nullable WriteContentDocument writeContent, @Nullable Object cacheKey) {
        this.document = document;
        this.filter = filter;
        this.writeContent = writeContent;
        this.cacheKey = cacheKey;
    }
}
