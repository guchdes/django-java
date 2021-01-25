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

import com.mongodb.MongoException;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import com.mountsea.django.core.exception.DjangoException;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @see #doWithNewSession
 * @see #doWithNewSessionTx
 * @author guch
 * @since 3.0.0
 */
public class ThreadLocalSessionMongoDatabase extends SessionBindingMongoDatabase {

    /**
     * 用来判断当前是否有任意线程开启了threadLocalSession，为0时不需要从threadLocal取session，
     * 大于0时所有线程都需要从threadLocal取session（虽然可能为null），等于0时当前线程一定没有使用session。
     * 使用此变量是为了做一点优化，避免每次都查询threadLocal。
     */
    private final AtomicInteger sessionCounter = new AtomicInteger();

    private final ThreadLocal<ClientSession> clientSessionThreadLocal = new ThreadLocal<>();

    public ThreadLocalSessionMongoDatabase(MongoDatabase delegated) {
        super(delegated, superRef -> {
            ThreadLocalSessionMongoDatabase database = (ThreadLocalSessionMongoDatabase) superRef;
            return () -> {
                if (database.sessionCounter.get() > 0) {
                    return database.clientSessionThreadLocal.get();
                } else {
                    return null;
                }
            };
        });
    }

    /**
     * 为当前线程开启一个threadLocalSession，执行动作，然后关闭session。
     * 在此期间当前线程调用此对象代理的mongoDatabase和mongoCollection的所有方法都会使用此session。
     *
     * @param clientSessionSupplier 提供session
     * @param action                session内执行动作，并返回结果
     */
    public <T> T doWithNewSession(Supplier<ClientSession> clientSessionSupplier, Supplier<T> action) {
        if (clientSessionThreadLocal.get() != null) {
            throw new DjangoException("Already has thread local session");
        }
        sessionCounter.incrementAndGet();
        AtomicReference<T> ret = new AtomicReference<>();
        ClientSession clientSession = null;
        try {
            clientSession = clientSessionSupplier.get();
            clientSessionThreadLocal.set(clientSession);
            ret.set(action.get());
        } finally {
            if (clientSession != null) {
                clientSession.close();
                clientSessionThreadLocal.set(null);
            }
            sessionCounter.decrementAndGet();
        }
        return ret.get();
    }

    /**
     * 为当前线程开启一个threadLocalSession，并开启事务，执行动作，然后关闭session。
     * 在此期间当前线程调用此对象代理的mongoDatabase和mongoCollection的所有方法都会使用此session。
     *
     * @param clientSessionSupplier 提供session
     * @param action                session内执行动作，并返回结果
     */
    public <T> T doWithNewSessionTx(Supplier<ClientSession> clientSessionSupplier,
                                    @Nullable TransactionOptions transactionOptions, Supplier<T> action) {
        if (clientSessionThreadLocal.get() != null) {
            throw new DjangoException("Already has thread local session");
        }
        sessionCounter.incrementAndGet();
        AtomicReference<T> ret = new AtomicReference<>();
        ClientSession clientSession = null;
        try {
            clientSession = clientSessionSupplier.get();
            if (transactionOptions == null) {
                clientSession.startTransaction();
            } else {
                clientSession.startTransaction(transactionOptions);
            }
            clientSessionThreadLocal.set(clientSession);
            ret.set(action.get());
            clientSession.commitTransaction();
        } catch (MongoException e) {
            if (clientSession != null && clientSession.hasActiveTransaction()) {
                clientSession.abortTransaction();
            }
        } finally {
            if (clientSession != null) {
                clientSession.close();
                clientSessionThreadLocal.set(null);
            }
            sessionCounter.decrementAndGet();
        }
        return ret.get();
    }


}
