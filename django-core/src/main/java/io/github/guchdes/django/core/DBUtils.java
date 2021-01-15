/**
 * MIT License
 *
 * Copyright (c) 2021 fengniao studio
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

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import io.github.guchdes.django.core.exception.DjangoException;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author guch
 * @Since 3.0.0
 */
class DBUtils {
    public static final UpdateOptions UPSERT_OPTION;

    static {
        UPSERT_OPTION = new UpdateOptions();
        UPSERT_OPTION.upsert(true);
    }

    @Nullable
    public static <T> T getSingleResult(FindIterable<T> iterable) {
        return getSingleResult(iterable, x -> x);
    }

    @Nullable
    public static <T, R> R getSingleResult(FindIterable<T> iterable, Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        FindIterable<T> findIterable = iterable.limit(1);
        MongoCursor<T> iterator = findIterable.iterator(); // 执行查询
        while (iterator.hasNext()) {
            T next = iterator.next();
            return mapper.apply(next);
        }
        return null;
    }

    @Nullable
    public static <T> T getSingleResultNotNull(FindIterable<T> iterable) throws DjangoException {
        return getSingleResultNotNull(iterable, x -> x);
    }

    @Nullable
    public static <T, R> R getSingleResultNotNull(FindIterable<T> iterable, Function<? super T, ? extends R> mapper) throws DjangoException {
        Objects.requireNonNull(mapper);
        FindIterable<T> findIterable = iterable.limit(1);
        for (T t : findIterable) {
            return mapper.apply(t);
        }
        throw new DjangoException("no result");
    }

    public static <T> List<T> toList(FindIterable<T> iterable) {
        return toList(iterable, x -> x);
    }

    public static <T, R> List<R> toList(FindIterable<T> iterable, Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        MongoCursor<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            List<R> list = new ArrayList<>();
            while (iterator.hasNext()) {
                T next = iterator.next();
                list.add(mapper.apply(next));
            }
            return list;
        } else {
            return new ArrayList<>(0);
        }
    }

}
