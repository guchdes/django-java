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
package com.mountsea.django.bson.projection;

import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * @author guch
 * @since 3.0.0
 */
public class SyncMongoUpdateCollector extends MongoUpdateCollectorImpl {

    private volatile boolean enable;

    public boolean hasEnableUpdateCollect() {
        return enable;
    }

    public void disableUpdateCollect() {
        synchronized (getLock()) {
            enable = false;
            super.clearUpdate();
        }
    }

    public void enableUpdateCollect() {
        enable = true;
    }

    protected Object getLock() {
        return this;
    }

    @Nullable
    @Override
    public Bson getUpdate() {
        synchronized (getLock()) {
            return super.getUpdate();
        }
    }

    @Nullable
    @Override
    public Bson getUpdate(Set<String> excludePaths) {
        synchronized (getLock()) {
            return super.getUpdate(excludePaths);
        }
    }

    @Nullable
    @Override
    public MongoUpdate getMongoUpdate() {
        synchronized (getLock()) {
            return super.getMongoUpdate();
        }
    }

    @Override
    public void clearUpdate() {
        synchronized (getLock()) {
            super.clearUpdate();
        }
    }

    @Override
    protected void recordUpdate(Op op, String path, Object fieldValue, Object previousValue,
                                boolean collectionBatchOp, Object collectionOpValue, Collection<?> collectionBatchOpValues) {
        synchronized (getLock()) {
            super.recordUpdate(op, path, fieldValue, previousValue, collectionBatchOp, collectionOpValue, collectionBatchOpValues);
        }
    }

}
