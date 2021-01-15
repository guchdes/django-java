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
package io.github.guchdes.django.bson.util;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 高性能的反射调用实现, 将反射调用转换为接口调用, 接口对象的内部实现和lambda对象一样.
 * <p>
 * 代理接口对象的创建有一定的开销, 应该在初始化的时候创建并保存下来. 如果是调用实例方法, 而且每次调用要被不同的对象接收,
 * 应该用 {@link #getInstanceMethodInvoker} 创建代理接口, 接口方法的第一个参数是接收方法调用的对象. 如果被代理的方法是
 * 静态方法或调用接收对象固定的实例方法, 用{@link #getMethodInvoker} 创建代理接口.
 * <p>
 * 示例:
 * List myList = new ArrayList();
 * IntSupplier fun = myList::size 相当于
 * IntSupplier fun = getMethodInvoker(List.class.getMethod("size"), myList, IntSupplier.class)
 * <p>
 * interface IListSize extends IntFunction<List>{}
 * IListSize fun = List::size 相当于
 * IListSize fun = getInstanceMethodInvoker(List.class.getMethod("size"), IListSize.class)
 * <p>
 * // TODO 添加类型检查
 *
 * @Author Create by jxz
 * @Date 2019/3/22
 */
public class LambdaUtils {

    /**
     * 创建静态方法或接收调用对象固定的实例方法的代理
     *
     * @param implMethod 实现方法, 可以是静态方法或实例方法
     * @param receiver   实例方法的调用接收对象, 如果实现方法是静态的, 必须传null, 否则必须不为null
     * @param toBeImplI  要实现的接口Class, 只能有一个非default方法, 该方法即将被实现的接口方法
     * @param <T>        接口类
     * @return 接口实现对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getMethodInvoker(Method implMethod, Object receiver, Class<T> toBeImplI) {
        Method interfaceMethod = getInterfaceFuncMethod(toBeImplI);
        boolean isStatic = Modifier.isStatic(implMethod.getModifiers());
        if (isStatic ^ receiver == null) throw new IllegalArgumentException();

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandle handle = lookup.unreflect(implMethod);
            MethodType invokeType = receiver == null ? MethodType.methodType(toBeImplI)
                    : MethodType.methodType(toBeImplI, receiver.getClass());
            // TODO 考虑CallSite缓存
            CallSite site = LambdaMetafactory.metafactory(
                    lookup, interfaceMethod.getName(), invokeType,
                    MethodType.methodType(interfaceMethod.getReturnType(),
                            interfaceMethod.getParameterTypes()),
                    handle, MethodType.methodType(implMethod.getReturnType(),
                            implMethod.getParameterTypes()));
            return (T) (receiver == null ? site.getTarget().invoke() : site.getTarget().invoke(receiver));
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * 创建实例方法的代理, 代理接口的第一个参数是方法调用的接收对象.
     *
     * @param implMethod 实现方法, 必须是非静态的
     * @param toBeImplI  要实现的接口Class, 只能有一个非default方法, 该方法即将被实现的接口方法
     * @param <T>        接口类
     * @return 接口实现对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstanceMethodInvoker(Method implMethod, Class<T> toBeImplI) {
        boolean isStatic = Modifier.isStatic(implMethod.getModifiers());
        if (isStatic) throw new IllegalArgumentException();

        Method interfaceMethod = getInterfaceFuncMethod(toBeImplI);

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandle handle = lookup.unreflect(implMethod);
            Class<?>[] methodParameterTypes = implMethod.getParameterTypes();
            Class<?>[] instantiatedPTypes = new Class<?>[methodParameterTypes.length + 1];
            instantiatedPTypes[0] = implMethod.getDeclaringClass();
            System.arraycopy(methodParameterTypes, 0, instantiatedPTypes, 1, methodParameterTypes.length);

            CallSite site = LambdaMetafactory.metafactory(
                    lookup, interfaceMethod.getName(),
                    MethodType.methodType(toBeImplI),
                    MethodType.methodType(interfaceMethod.getReturnType(),
                            interfaceMethod.getParameterTypes()),
                    handle, MethodType.methodType(implMethod.getReturnType(),
                            instantiatedPTypes));
            return (T) site.getTarget().invoke();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * 获取无参构造函数的代理
     *
     * @param aClass 类
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> ThrowingSupplier<T, Exception> getDefaultConstructorInvoker(Class<T> aClass) {
        Constructor<T> constructor = null;
        try {
            constructor = aClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("no default Constructor :" + aClass);
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandle handle = lookup.unreflectConstructor(constructor);
            CallSite site = LambdaMetafactory.metafactory(
                    lookup, SUPPLIER_GET_M.getName(),
                    MethodType.methodType(ThrowingSupplier.class),
                    MethodType.methodType(SUPPLIER_GET_M.getReturnType()),
                    handle,
                    MethodType.methodType(aClass));
            return (ThrowingSupplier<T, Exception>) site.getTarget().invoke();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 创建任意参数的构造方法的代理
     *
     * @param constructor 构造方法
     * @param toBeImplI   要实现的接口类
     * @param <T>         接口类
     * @return 接口实现对象
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T getConstructorInvoker(Constructor constructor, Class<T> toBeImplI) {
        Method interfaceMethod = getInterfaceFuncMethod(toBeImplI);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandle handle = lookup.unreflectConstructor(constructor);
            CallSite site = LambdaMetafactory.metafactory(
                    lookup, interfaceMethod.getName(),
                    MethodType.methodType(toBeImplI),
                    MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes()),
                    handle,
                    MethodType.methodType(constructor.getDeclaringClass(), constructor.getParameterTypes()));
            return (T) site.getTarget().invoke();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * @param iClass 接口类, 必须有且只有一个需要实现(非default)的方法, 也可以只有一个default方法
     * @return 接口的唯一的需要实现的方法
     */
    private static Method getInterfaceFuncMethod(Class<?> iClass) {
        if (!iClass.isInterface()) {
            throw new IllegalArgumentException();
        }

        Method retM = null;
        Method defM = null;
        int defMCount = 0;

        List<Method> list = getAllInstanceMethods(iClass);

        for (Method method : list) {
            if (method.isDefault()) {
                defMCount++;
                defM = method;
            } else {
                if (retM == null) {
                    retM = method;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }

        if (retM == null && defMCount != 1) {
            throw new IllegalArgumentException();
        }
        return retM == null ? defM : retM;
    }

    private static final Method SUPPLIER_GET_M;

    static {
        SUPPLIER_GET_M = getInterfaceFuncMethod(ThrowingSupplier.class);
    }

    /**
     * 获取aClass的所有实例方法, 包括父类的方法, 不含被重写的方法
     *
     * @param aClass 可以是普通类或接口类
     */
    private static List<Method> getAllInstanceMethods(Class<?> aClass) {
        List<Class<?>> classList = aClass.isInterface() ?
                org.apache.commons.lang3.ClassUtils.getAllInterfaces(aClass) :
                org.apache.commons.lang3.ClassUtils.getAllSuperclasses(aClass);
        classList.add(aClass);

        class SigKey {
            String name;
            Class<?>[] params;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                SigKey sigKey = (SigKey) o;
                return Objects.equals(name, sigKey.name) &&
                        Arrays.equals(params, sigKey.params);
            }

            @Override
            public int hashCode() {
                int result = Objects.hash(name);
                result = 31 * result + Arrays.hashCode(params);
                return result;
            }
        }

        Map<SigKey, Method> map = new HashMap<>();
        for (Class<?> aClass1 : classList) {
            Method[] methods = aClass1.getMethods();
            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers())) continue;
                SigKey sigKey = new SigKey();
                sigKey.name = method.getName();
                sigKey.params = method.getParameterTypes();
                Method method1 = map.get(sigKey);
                if (method1 == null ||
                        isMethodOverride(method, method1)) {
                    map.put(sigKey, method);
                }
            }
        }

        return new ArrayList<>(map.values());
    }

    /**
     * 是否method1重写了method2
     */
    private static boolean isMethodOverride(Method method1, Method method2) {
        return method1.getName().equals(method2.getName()) &&
                !method1.getDeclaringClass().equals(method2.getDeclaringClass()) &&
                method2.getDeclaringClass().isAssignableFrom(method1.getDeclaringClass()) &&
                method2.getReturnType().isAssignableFrom(method1.getReturnType()) &&
                Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes());
    }

}
