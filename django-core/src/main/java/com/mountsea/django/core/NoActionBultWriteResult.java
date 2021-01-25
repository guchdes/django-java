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

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.bulk.BulkWriteUpsert;

import java.util.Collections;
import java.util.List;

/**
 * @author guch
 * @since 3.0.0
 */
public class NoActionBultWriteResult extends BulkWriteResult {
    @Override
    public boolean wasAcknowledged() {
        return true;
    }

    @Override
    public int getInsertedCount() {
        return 0;
    }

    @Override
    public int getMatchedCount() {
        return 0;
    }

    @Override
    public int getDeletedCount() {
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isModifiedCountAvailable() {
        return false;
    }

    @Override
    public int getModifiedCount() {
        return 0;
    }

    @Override
    public List<BulkWriteUpsert> getUpserts() {
        return Collections.emptyList();
    }
}
