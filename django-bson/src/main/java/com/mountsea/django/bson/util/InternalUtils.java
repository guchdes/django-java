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
package com.mountsea.django.bson.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class InternalUtils {
    private static final int MAJOR_JAVA_VERSION;

    static {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        MAJOR_JAVA_VERSION = Integer.parseInt(version);
    }

    /**
     * JDK bug:  https://bugs.openjdk.java.net/browse/JDK-8062841
     * 递归调用ConcurrentHashMap#computeIfAbsent可能进入死循环.
     * <p>
     * 如果可能递归, 用此方法作为替代.
     */
    public static <K, V> V mapComputeIfAbsent(Map<K, V> map, K key, Function<? super K, ? extends V> mappingFunction) {
        if (map instanceof ConcurrentHashMap && MAJOR_JAVA_VERSION <= 8) {
            Objects.requireNonNull(mappingFunction);
            V v;
            if ((v = map.get(key)) == null) {
                V newValue;
                if ((newValue = mappingFunction.apply(key)) != null) {
                    map.put(key, newValue);
                    return newValue;
                }
            }
            return v;
        } else {
            return map.computeIfAbsent(key, mappingFunction);
        }
    }

    // 获取Class或ParameterizedType中的Class (ParameterizedType的rawType一定是Class)
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
            if (!(type instanceof Class)) throw new InternalError();
            return (Class<?>) type;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 获取所有符合条件的Field，先从子类找，如果没有则向上查找父类，
     * 如果有一个类中有一个或多个匹配字段，则不再查找父类。
     */
    public static List<Field> findFieldsUpward(Class<?> c, Predicate<Field> predicate) {
        while (c != null) {
            List<Field> list = null;
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                if (predicate.test(field)) {
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(field);
                }
            }
            if (list != null) {
                return list;
            }
            c = c.getSuperclass();
        }
        return Collections.emptyList();
    }

    /**
     * 是否otherPath是和path相同的路径或是path的下级路径
     */
    public static boolean isEqOrSubPath(String path, String otherPath) {
        return path.equals(otherPath) || otherPath.startsWith(path + ".");
    }

    /**
     * 是否otherPath是path的下级路径
     */
    public static boolean isSubPath(String path, String otherPath) {
        return otherPath.startsWith(path + ".");
    }
}
