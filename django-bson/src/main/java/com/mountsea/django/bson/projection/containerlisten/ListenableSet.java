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
package com.mountsea.django.bson.projection.containerlisten;

import java.util.*;

/**
 * @author guch
 * @since 3.0.0
 */
public class ListenableSet<E> extends ListenableCollection<E> implements Set<E> {

    public ListenableSet(Set<E> set, SetListener<E> listener) {
        super(set, listener);
    }

    /**
     * @param isSync 是否对操作collection和调用listener加锁
     */
    public ListenableSet(Set<E> set, SetListener<E> listener, boolean isSync) {
        super(set, listener, isSync);
    }

    ListenableSet(Set<E> set, SetListener<E> listener, ChangeLock changeLock, Object lock) {
        super(set, listener, changeLock, lock);
    }

    public SetListener<E> getListener() {
        return (SetListener<E>) listener;
    }

    /**
     * 只传递添加成功的元素给listener.
     * <p>
     * 调用Set.add，如果元素已存在，则返回false，且Set中的原对象不会被替换。
     * 包括用Map实现的Set，也是如此，Map.put时如果Entry已存在，不会替换Key对象。
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        Objects.requireNonNull(c);
        return doWithThreadLock(() -> {
            return changeLock.doWithChangeLock((hasLock) -> {
                List<E> addList = null;
                for (E e : c) {
                    e = transform(e, hasLock);
                    boolean add = collection.add(e);
                    if (add) {
                        if (addList == null) {
                            addList = new ArrayList<>();
                        }
                        addList.add(e);
                    }
                }
                if (addList != null && hasLock) {
                    listener.afterAddAll(getThis(), addList, true);
                }
                return addList != null;
            });
        });
    }

    /**
     * 使用 {@link AbstractSet#equals(java.lang.Object)}的实现代码
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Set))
            return false;
        Collection<?> c = (Collection<?>) o;
        if (c.size() != size())
            return false;
        try {
            return containsAll(c);
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

}
