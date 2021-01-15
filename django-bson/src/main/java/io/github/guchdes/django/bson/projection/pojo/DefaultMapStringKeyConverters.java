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
package io.github.guchdes.django.bson.projection.pojo;

import io.github.guchdes.django.bson.projection.IndexedEnum;
import io.github.guchdes.django.bson.projection.MapStringKeyConvertable;
import org.bson.types.ObjectId;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class DefaultMapStringKeyConverters {
    public static ExternalMapStringKeyConverter<String> STRING_CONVERTER = new ExternalMapStringKeyConverter<String>() {
        @Override
        public String toString(String v) {
            return v;
        }

        @Override
        public String fromString(String s) {
            return s;
        }
    };


    public static ExternalMapStringKeyConverter<ObjectId> OBJECT_ID_CONVERTER = new ExternalMapStringKeyConverter<ObjectId>() {
        @Override
        public String toString(ObjectId v) {
            return v.toHexString();
        }

        @Override
        public ObjectId fromString(String s) {
            return new ObjectId(s);
        }
    };

    public static ExternalMapStringKeyConverter<Integer> INTEGER_CONVERTER = new ExternalMapStringKeyConverter<Integer>() {
        @Override
        public String toString(Integer v) {
            return v.toString();
        }

        @Override
        public Integer fromString(String s) {
            return Integer.valueOf(s);
        }
    };

    public static ExternalMapStringKeyConverter<Long> LONG_CONVERTER = new ExternalMapStringKeyConverter<Long>() {
        @Override
        public String toString(Long v) {
            return v.toString();
        }

        @Override
        public Long fromString(String s) {
            return Long.valueOf(s);
        }
    };

    public static <E extends Enum<E>> ExternalMapStringKeyConverter<E> createEnumConverter(Class<E> enumClass) {
        if (!Enum.class.isAssignableFrom(enumClass)) {
            throw new IllegalArgumentException();
        }
        boolean isIndexEnum = IndexedEnum.class.isAssignableFrom(enumClass);
        Map<String, E> indexMap = Arrays.stream(enumClass.getEnumConstants()).collect(
                        Collectors.toMap(x -> isIndexEnum ? ((IndexedEnum) x).getIndex() + "" : x.name(), x -> x));

        return new ExternalMapStringKeyConverter<E>() {
            @Override
            public String toString(E v) {
                Objects.requireNonNull(v);
                return isIndexEnum ? ((IndexedEnum) v).getIndex() + "" : v.name();
            }

            @Override
            public E fromString(String s) {
                Objects.requireNonNull(s);
                E e = indexMap.get(s);
                if (e == null) {
                    throw new IllegalArgumentException("Not found enum " + s + " of " + enumClass);
                }
                return e;
            }
        };
    }

    public static <T extends MapStringKeyConvertable<T>> ExternalMapStringKeyConverter<T> createFromStringConvertable(Class<T> tClass) {
        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator<T> instantiator = objenesis.getInstantiatorOf(tClass);
        return new ExternalMapStringKeyConverter<T>() {
            @Override
            public String toString(T v) {
                return v.toStringKey();
            }

            @Override
            public T fromString(String s) {
                T t = instantiator.newInstance();
                return t.fromStringKey(s);
            }
        };
    }

}
