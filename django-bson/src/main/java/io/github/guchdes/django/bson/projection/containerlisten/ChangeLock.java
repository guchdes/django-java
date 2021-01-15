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
package io.github.guchdes.django.bson.projection.containerlisten;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 为了避免执行同一次容器操作时, 在不同的方法里面多次重复调用Listener, 在执行操作时先获取锁(不是线程锁).
 * 得到锁才调用Listener, 没得到锁不调用Listener, 无论是否得到锁, 容器操作正常进行.
 *
 * @Author guch
 * @Since 3.0.0
 */
class ChangeLock {

    private boolean locked;

    public boolean tryLock() {
        if (locked) {
            return false;
        } else {
            locked = true;
            return true;
        }
    }

    public void tryUnlock() {
        locked = false;
    }

    public interface Action<R> {
        R run(boolean hasLock);
    }

    public <T> T doWithChangeLock(Action<T> action) {
        boolean tryLock = tryLock();
        T r;
        try {
            r = action.run(tryLock);
        } finally {
            if (tryLock) {
                tryUnlock();
            }
        }
        return r;
    }

    /**
     * @param changeAction     返回是否有改变
     * @param callListenerOnce 如果有改变且获得了锁, 则调用Listener. 如果没获得锁, 不会调用Listener.
     * @return 是否有改变 (changeAction的返回值)
     */
    public boolean doWithChangeLock(BooleanSupplier changeAction, Runnable callListenerOnce) {
        boolean tryLock = tryLock();
        boolean change;
        try {
            change = changeAction.getAsBoolean();
            if (tryLock && change) {
                callListenerOnce.run();
            }
        } finally {
            if (tryLock) {
                tryUnlock();
            }
        }
        return change;
    }

    /**
     * @param changeAction     返回此方法(doChangeWithLock)需要返回的值
     * @param callListenerOnce 如果获得了锁就调用
     * @param <T>
     * @return changeAction的返回值
     */
    public <T> T doWithChangeLock(Supplier<T> changeAction, Runnable callListenerOnce) {
        boolean tryLock = tryLock();
        T r;
        try {
            r = changeAction.get();
            if (tryLock) {
                callListenerOnce.run();
            }
        } finally {
            if (tryLock) {
                tryUnlock();
            }
        }
        return r;
    }

    /**
     * @param changeAction                  返回此方法(doChangeWithLock)需要返回的值
     * @param callListenerOnceAndPassResult 如果获得了锁就调用，调用时传递action返回的值
     * @param <T>
     * @return changeAction的返回值
     */
    public <T> T doWithChangeLock(Supplier<T> changeAction, Consumer<T> callListenerOnceAndPassResult) {
        boolean tryLock = tryLock();
        T r;
        try {
            r = changeAction.get();
            if (tryLock) {
                callListenerOnceAndPassResult.accept(r);
            }
        } finally {
            if (tryLock) {
                tryUnlock();
            }
        }
        return r;
    }

}
