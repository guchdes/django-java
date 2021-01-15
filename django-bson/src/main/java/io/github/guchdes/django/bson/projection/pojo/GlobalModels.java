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

import io.github.guchdes.django.bson.annotation.FillNullByEmpty;
import io.github.guchdes.django.bson.projection.DocumentNode;
import io.github.guchdes.django.bson.projection.ProxiedDocumentCreator;

import java.util.List;

import static io.github.guchdes.django.bson.projection.pojo.GlobalModelsImpl.INSTANCE;

/**
 * 获取/注册类结构等信息。
 *
 * @Author guch
 * @Since 3.0.0
 */
public class GlobalModels {

    /**
     * 注册ImmutableType
     */
    public static void regImmutableType(Class<?> aClass, boolean isImmutable) {
        INSTANCE.regImmutableType(aClass, isImmutable);
    }

    /**
     * @return 是否ImmutableType
     */
    public static boolean isImmutableType(Class<?> aClass) {
        return INSTANCE.isImmutableType(aClass);
    }

    /**
     * @return 是否简单类型，简单类型不能是容器，且不含有可读取的类字段
     */
    public static boolean isSimpleType(Class<?> aClass) {
        return INSTANCE.isSimpleType(aClass);
    }

    /**
     * 注册Map的Key转换String类型的方法
     *
     * @see io.github.guchdes.django.bson.projection.MapStringKeyConvertable
     */
    public static <T> void regMapStringKeyConverter(Class<T> aClass, ExternalMapStringKeyConverter<T> converter) {
        INSTANCE.regMapStringKeyConverter(aClass, converter);
    }

    public static <T> ExternalMapStringKeyConverter<T> getOrCreateStringKeyConverter(Class<T> k) {
        return INSTANCE.getOrCreateStringKeyConverter(k);
    }

    /**
     * 获取ClassModel，不存在则创建，并缓存结果
     */
    public static <T> ClassModel<T> getClassModel(Class<T> aClass) {
        return INSTANCE.getClassModel(aClass);
    }

    /**
     * 获取DocumentNode填充器
     */
    public static <T extends DocumentNode> DocumentNodeFiller<T> getDocumentNodeFiller(Class<T> tClass) {
        return INSTANCE.getDocumentNodeFiller(tClass);
    }

    /**
     * 获取一个类的所有属性信息
     */
    public static List<PropertyModel<?>> createPropertyModels(Class<?> aClass) {
        return INSTANCE.createPropertyModels(aClass);
    }

    /**
     * @return 是否容器类型
     */
    public static boolean isContainerType(Class<?> aClass) {
        return INSTANCE.isContainerType(aClass);
    }

    /**
     * 是否CGlib代理类
     */
    public static boolean isProxyClass(Class<?> aClass) {
        return INSTANCE.isProxyClass(aClass);
    }

    /**
     * 如果是CGLib代理类，返回被代理类，否则返回参数本身
     */
    public static Class<?> getCGLibProxyRawClass(Class<?> aClass) {
        return INSTANCE.getCGLibProxyRawClass(aClass);
    }

    public static boolean isFillNullByEmpty(Class<?> aClass) {
        return aClass.getAnnotation(FillNullByEmpty.class) != null;
    }

    public static boolean hasClassEnhancer(Class<?> aClass) {
        return INSTANCE.hasClassEnhancer(aClass);
    }

    public static <T extends DocumentNode> ProxiedDocumentCreator<T> getProxiedDocumentCreator(Class<T> aClass) {
        return INSTANCE.getProxiedDocumentCreator(aClass);
    }
}
