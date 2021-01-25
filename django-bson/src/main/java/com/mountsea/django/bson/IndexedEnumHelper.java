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
package com.mountsea.django.bson;

import com.mountsea.django.bson.projection.IndexedEnum;
import com.mountsea.django.bson.util.LazyInitializer;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IndexedEnumHelper {

    public interface IndexToEnumFun<T extends Enum<T> & IndexedEnum> extends Function<Integer, T> {
    }

    public static <T extends Enum<T> & IndexedEnum>
    IndexToEnumFun<T> getIndexToEnumConverter(Class<T> enumClass) {
        LazyInitializer<Function<Integer, T>> initializer = new LazyInitializer<>(() -> {
            // 注意x.getIndex不能改成方法引用，因为x会被当做Enum类型，没有getIndex方法，运行时会报错
            Map<Integer, T> map = Arrays.stream(enumClass.getEnumConstants()).collect(Collectors.toMap(
                    x -> x.getIndex(), x -> x));
            return integer -> {
                T t = map.get(integer);
                if (t == null) {
                    throw new IllegalArgumentException("Not find enum index " + integer + " in " + enumClass);
                }
                return t;
            };
        });
        return integer -> initializer.get().apply(integer);
    }
}
