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

import com.mountsea.django.bson.projection.containerlisten.LinkedHashSet2;
import com.mountsea.django.bson.projection.containerlisten.ListenableSet;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 使用LinkedHashSet做为实现类
 * <p>
 * 非线程安全
 *
 * @author guch
 * @since 3.0.0
 */
@NotThreadSafe
public class LinkedDocumentSet<E> extends DocumentSet<E> implements Set<E> {

    public LinkedDocumentSet() {
        this(node -> {
            return new ListenableSet<>(new LinkedHashSet2<>(), new ProxySetListener<>(node), false);
        });
    }

    public LinkedDocumentSet(Collection<? extends E> collection) {
        this(node -> {
            return new ListenableSet<>(new LinkedHashSet2<>(collection), new ProxySetListener<>(node), false);
        });
    }

    protected LinkedDocumentSet(ListenableCollectionCreator creator) {
        super(creator);
    }

    @SuppressWarnings("unchecked")
    public LinkedHashSet<E> cloneToLinkedHashSet() {
        return (LinkedHashSet<E>) deepCloneCollection(() -> new HashSet<>(size()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public LinkedDocumentSet<E> deepCloneSelf() {
        return (LinkedDocumentSet<E>) super.deepCloneSelf();
    }

}
