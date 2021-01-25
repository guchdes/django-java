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

import com.mountsea.django.bson.util.LambdaUtils;
import com.mountsea.django.bson.util.ThrowingSupplier;
import net.sf.cglib.proxy.*;
import net.sf.cglib.reflect.FastClass;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClassEnhancer {

    private final Class<?> superClass;
    private final Enhancer enhancer;
    private final Class<?> enhancerClass;

    private final ThrowingSupplier<Object, Exception> defaultCreator;

    private final Callback[] callbacks;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ClassEnhancer(ClassEnhancerConfig classProxyConf) {
        superClass = classProxyConf.getConfigClass();
        enhancer = new Enhancer();
        enhancer.setSuperclass(superClass);
        enhancer.setCallbackFilter(method -> {
            return classProxyConf.interceptorMap.containsKey(method) ? 1 : 0;
        });
        //此处将所有MethodInterceptor合并为一个MethodInterceptor，因为CGLib会为每个Callback分配一个实例字段，
        //当Callback实例字段较多，又需要创建大量的代理对象时，会有更多的内存消耗。
        //此类的需求是所有代理对象使用相同的MethodInterceptor，所以可以优化为所有MethodInterceptor压缩到单个MethodInterceptor，
        //单个MethodInterceptor根据MethodProxy提供的index查找实际的MethodInterceptor以优化查找性能
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancerClass = enhancer.createClass();

        callbacks = new Callback[]{NoOp.INSTANCE, new MethodInterceptorImpl(enhancerClass, enhancer, classProxyConf.interceptorMap)};
        enhancer.setCallbacks(callbacks);

        // 初始化构造器
        boolean hasDefaultConstructor = false;
        try {
            enhancerClass.getConstructor();
            hasDefaultConstructor = true;
        } catch (NoSuchMethodException e) {
        }
        if (hasDefaultConstructor) {
            defaultCreator = (ThrowingSupplier<Object, Exception>) LambdaUtils.getDefaultConstructorInvoker(enhancerClass);
        } else {
            defaultCreator = null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T create() {
        //创建对象效率: lambda接口对象 > Factory > enhancer.create.
        if (defaultCreator == null) {
            throw new IllegalStateException("no default construct");
        }
        try {
            T t = (T) defaultCreator.get();
            ((Factory) t).setCallbacks(callbacks);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("create obj fail", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?>[] paramTypes, Object[] args) {
        return (T) enhancer.create(paramTypes, args);
    }

    public Class<?> getEnhancerClass() {
        return enhancerClass;
    }

    private static class MethodInterceptorImpl implements MethodInterceptor {

        private final MethodInterceptor[] interceptors;

        public MethodInterceptorImpl(Class<?> enhancerClass,
                                     Enhancer enhancer,
                                     Map<Method, MethodInterceptor> interceptorMap) {
            //根据MethodProxy提供的index查找实际的MethodInterceptor以优化查找性能
            FastClass.Generator g = new FastClass.Generator();
            g.setType(enhancerClass);
            g.setClassLoader(enhancerClass.getClassLoader());
            g.setNamingPolicy(enhancer.getNamingPolicy());
            g.setStrategy(enhancer.getStrategy());
            g.setAttemptLoad(enhancer.getAttemptLoad());
            FastClass fastClass = g.create();

            Map<Integer, MethodInterceptor> indexMap = new HashMap<>();
            for (Map.Entry<Method, MethodInterceptor> entry : interceptorMap.entrySet()) {
                Method key = entry.getKey();
                String callSuperMethodName = getCallSuperMethodName(enhancerClass, key);
                int index = fastClass.getIndex(callSuperMethodName, key.getParameterTypes());
                indexMap.put(index, entry.getValue());
            }
            int max = indexMap.keySet().stream().mapToInt(x -> x).max().orElse(0);
            interceptors = new MethodInterceptor[max + 1];
            for (Map.Entry<Integer, MethodInterceptor> entry : indexMap.entrySet()) {
                interceptors[entry.getKey()] = entry.getValue();
            }
        }

        private String getCallSuperMethodName(Class<?> enhancerClass, Method method) {
            Method[] methods = enhancerClass.getDeclaredMethods();
            for (Method method1 : methods) {
                String prefix = "CGLIB$" + method.getName() + "$";
                String name = method1.getName();
                if (!name.startsWith(prefix)) {
                    continue;
                }
                String suf = name.substring(prefix.length());
                boolean suffixIsNumber = true;
                try {
                    Integer.parseInt(suf);
                } catch (NumberFormatException e) {
                    suffixIsNumber = false;
                }
                if (suffixIsNumber && Arrays.equals(method1.getParameterTypes(), method.getParameterTypes()) &&
                        method1.getReturnType().equals(method.getReturnType())) {
                    return method1.getName();
                }
            }
            throw new IllegalStateException("Not found call super method for:" + method);
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            MethodInterceptor interceptor = interceptors[methodProxy.getSuperIndex()];
            return interceptor.intercept(o, method, objects, methodProxy);
        }
    }


}
