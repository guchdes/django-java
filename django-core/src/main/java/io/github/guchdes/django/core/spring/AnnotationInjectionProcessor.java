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
package io.github.guchdes.django.core.spring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 提供通过注解向bean的字段或setter注入对象的通用方式
 *
 * @Author guch
 */
public class AnnotationInjectionProcessor implements BeanPostProcessor {

    /**
     * 表示根据一个注解生成一个注入对象的处理器
     *
     * @param <T> 注解类型
     * @param <O> 注入对象类型
     */
    public interface InjectionProvider<T extends Annotation, O> {
        /**
         * @param bean             被注入的bean
         * @param annotatedElement 被注解的字段或setter方法
         * @param annotation       注解
         * @return 返回要注入的对象
         */
        O provideInjectionObject(Object bean, AnnotatedElement annotatedElement, Class<?> targetType, T annotation);
    }

    /**
     * 添加一个要处理的注解类型
     *
     * @param annotationClass 注解类型
     * @param targetType      目标类型
     * @param provider        对应此注解的注入对象provider
     * @param <T>
     */
    public <T extends Annotation, O> void add(Class<T> annotationClass, Class<O> targetType,
                                              InjectionProvider<T, O> provider) {
        injections.add(new InjectionEntry(annotationClass, provider, aClass -> aClass.isAssignableFrom(targetType)));
    }

    /**
     * 添加一个要处理的注解类型
     *
     * @param annotationClass     注解类型
     * @param targetTypePredicate 判断目标类型是否符合条件
     * @param provider            对应此注解的注入对象provider
     * @param <T>
     */
    public <T extends Annotation, O> void add(Class<T> annotationClass, Predicate<Class<?>> targetTypePredicate,
                                              InjectionProvider<T, O> provider) {
        injections.add(new InjectionEntry(annotationClass, provider, targetTypePredicate));
    }

    @Getter
    @AllArgsConstructor
    private static class InjectionEntry {
        final Class<? extends Annotation> annotationClass;
        final InjectionProvider<?, ?> injectionProvider;
        final Predicate<Class<?>> targetTypePredicate;
    }

    private final List<InjectionEntry> injections = new ArrayList<>();

    public AnnotationInjectionProcessor() {
    }

    @SuppressWarnings("unchecked")
    private <T> T stripBeanProxy(T bean) {
        if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
            try {
                return (T) ((Advised) bean).getTargetSource().getTarget();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getBeanOriginalClass(T bean) {
        bean = stripBeanProxy(bean);
        if (bean.getClass().getName().contains("$$EnhancerBySpringCGLIB")) {
            return (Class<T>) bean.getClass().getSuperclass();
        } else {
            return (Class<T>) bean.getClass();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (injections.isEmpty()) {
            return bean;
        }
        bean = stripBeanProxy(bean);
        Class<?> clazz = getBeanOriginalClass(bean);
        final Object bean0 = bean;
        do {
            ReflectionUtils.doWithFields(clazz, field -> {
                for (InjectionEntry injectionEntry : injections) {
                    InjectionProvider<Annotation, ?> provider = (InjectionProvider<Annotation, ?>)
                            injectionEntry.getInjectionProvider();
                    Predicate<Class<?>> targetTypePredicate = injectionEntry.getTargetTypePredicate();
                    Class<? extends Annotation> annotationClass = injectionEntry.getAnnotationClass();
                    final Annotation annotation = AnnotationUtils.findAnnotation(field, annotationClass);
                    if (annotation != null) {
                        if (!targetTypePredicate.test(field.getType())) {
                            throw new BeanInitializationException(
                                    "Can not use Annotation " + annotationClass.getName() + " on field " + field
                                            + " because of target type incompatible");
                        }
                        Object injectionObject = provider.provideInjectionObject(bean0, field, field.getType(), annotation);
                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, bean0, injectionObject);
                    }
                }
            });
            ReflectionUtils.doWithMethods(clazz, method -> {
                for (InjectionEntry injectionEntry : injections) {
                    InjectionProvider<Annotation, ?> provider = (InjectionProvider<Annotation, ?>)
                            injectionEntry.getInjectionProvider();
                    Predicate<Class<?>> targetTypePredicate = injectionEntry.getTargetTypePredicate();
                    Class<? extends Annotation> annotationClass = injectionEntry.getAnnotationClass();
                    final Annotation annotation = AnnotationUtils.findAnnotation(method, annotationClass);
                    if (annotation != null) {
                        final Type[] paramTypes = method.getGenericParameterTypes();
                        if (paramTypes.length != 1) {
                            throw new BeanInitializationException(
                                    "Method " + method + " doesn't have exactly one parameter.");
                        }
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (!targetTypePredicate.test(parameterTypes[0])) {
                            throw new BeanInitializationException(
                                    "Can not use Annotation " + annotationClass.getName() + " on method " + method
                                            + " because of target type incompatible");
                        }
                        Object injectionObject = provider.provideInjectionObject(bean0, method, parameterTypes[0], annotation);
                        ReflectionUtils.makeAccessible(method);
                        ReflectionUtils.invokeMethod(method, bean0, injectionObject);
                    }
                }
            });
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return bean;
    }

    @Override
    public final Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
