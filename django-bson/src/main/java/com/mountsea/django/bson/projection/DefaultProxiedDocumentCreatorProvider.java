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

import com.mountsea.django.bson.annotation.IgnoreRecordField;
import com.mountsea.django.bson.projection.dtbson.DtBsonArray;
import com.mountsea.django.bson.projection.dtbson.DtBsonDocument;
import com.mountsea.django.bson.projection.dtbson.DtBsonValue;
import com.mountsea.django.bson.projection.pojo.GlobalModels;
import com.mountsea.django.bson.projection.pojo.PropertyMetadata;
import com.mountsea.django.bson.projection.pojo.PropertyModel;
import com.mountsea.django.bson.util.InternalUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guch
 * @since 3.0.0
 */
@Slf4j
public class DefaultProxiedDocumentCreatorProvider implements ProxiedDocumentCreatorProvider {

    private final Map<Class<?>, ProxiedDocumentCreator<?>> map = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DocumentNode> ProxiedDocumentCreator<T> get(Class<T> aClass) {
        ProxiedDocumentCreator<?> creator = map.get(aClass);
        if (creator != null) {
            return (ProxiedDocumentCreator<T>) creator;
        }
        return (ProxiedDocumentCreator<T>) InternalUtils.mapComputeIfAbsent(map, aClass, this::createProxy);
    }

    private ProxiedDocumentCreator<?> createProxy(Class<?> aClass) {
        if (!GlobalModels.hasClassEnhancer(aClass)) {
            throw new DocumentClassDefinitionException("Cannot create ClassEnhancer :" + aClass);
        }

        List<PropertyModel<?>> propertyModels = GlobalModels.createPropertyModels(aClass);
        ClassEnhancerConfig classEnhancerConfig = new ClassEnhancerConfig(aClass);
        for (PropertyModel<?> propertyModel : propertyModels) {
            PropertyMetadata<?> propertyMetadata = propertyModel.getPropertyMetadata();
            Field field = propertyMetadata.getField();
            IgnoreRecordField recordIgnoreField = field.getAnnotation(IgnoreRecordField.class);
            if (recordIgnoreField != null) {
                continue;
            }

            Method setter = propertyMetadata.getSetter();
            field.setAccessible(true);

            checkPropertyType(propertyMetadata.getGenericType(), field);
            Class<?> type = propertyMetadata.getTypeData().getType();

            String name = propertyMetadata.getName();
            boolean isImmutable = isImmutable(type);
            boolean isDtBsonValue = DtBsonValue.class.isAssignableFrom(type);

            // 设置当前setter方法的代理
            classEnhancerConfig.addMethodInterceptor(setter, (obj, method, args, proxy) -> {
                return interceptSetter(obj, args, proxy, field, isImmutable, isDtBsonValue, name);
            });
        }

        ClassEnhancer classEnhancer = new ClassEnhancer(classEnhancerConfig);
        return new ProxiedDocumentCreator<DocumentNode>() {
            @Override
            public DocumentNode create() {
                return classEnhancer.create();
            }

            @Override
            public DocumentNode create(Class<?>[] paramTypes, Object[] args) {
                return classEnhancer.create(paramTypes, args);
            }
        };
    }

    private boolean isImmutable(Class<?> aClass) {
        return GlobalModels.isImmutableType(aClass);
    }

    private void checkPropertyType(Type type, Field field) {
        Class<?> aClass = InternalUtils.getRawType(type);
        if (DtBsonValue.class.isAssignableFrom(aClass)) {
            return;
        } else if (Enum.class.equals(aClass)) {
            throw new DocumentClassDefinitionException("Can not use base Enum type:" + field);
        } else if (Map.class.isAssignableFrom(aClass)) {
            if (!DocumentMap.class.isAssignableFrom(aClass)) {
                throw new DocumentClassDefinitionException("Illegal map type:" + field);
            }
            Type elementType = getMapElementType(type);
            if (elementType == null) {
                throw new DocumentClassDefinitionException("Illegal map element type:" + field);
            }
            checkPropertyType(elementType, field);
        } else if (Collection.class.isAssignableFrom(aClass)) {
            if (!ContainerDocumentNode.class.isAssignableFrom(aClass)) {
                throw new DocumentClassDefinitionException("Illegal collection type:" + field);
            }
            Type elementType = getCollectionElementType(type);
            if (elementType == null) {
                throw new DocumentClassDefinitionException("Illegal collection element type:" + field);
            }
            checkPropertyType(elementType, field);
        } else if (GlobalModels.isImmutableType(aClass)) {
            return;
        } else if (DocumentNode.class.isAssignableFrom(aClass)) {
            if (NotProxy.class.isAssignableFrom(aClass)) {
                throw new DocumentClassDefinitionException("Cannot using NotProxy type in DocumentNode field. " + field);
            } else if (CollectibleDocumentNode.class.isAssignableFrom(aClass) &&
                    !NotAsRecordRoot.class.isAssignableFrom(aClass)) {
                throw new DocumentClassDefinitionException("CollectibleDocumentNode must implement NotAsRecordRoot" +
                        " when using as field type. " + field);
            }
        } else {
            throw new DocumentClassDefinitionException("Illegal document type, not DocumentNode or immutable :" + aClass.getSimpleName() + " in  property:" + field);
        }
    }

    @Nullable
    private Type getMapElementType(Type type) {
        TypeVariable<?> typeVariable = Map.class.getTypeParameters()[1];
        return TypeUtils.getTypeArguments(type, Map.class).get(typeVariable);
    }

    @Nullable
    private Type getCollectionElementType(Type type) {
        TypeVariable<?> typeVariable = Collection.class.getTypeParameters()[0];
        return TypeUtils.getTypeArguments(type, Collection.class).get(typeVariable);
    }

    private boolean isImmutableObject(Object obj, boolean isImmutableType, boolean isDtBsonValueType) {
        if (obj == null) {
            return true;
        } else if (isDtBsonValueType) {
            return !(obj instanceof DtBsonDocument || obj instanceof DtBsonArray);
        } else {
            return isImmutableType;
        }
    }

    /**
     * 对字段赋值时，对象的类型必须和字段类型一致，不能是字段的子类型，只有 DtBsonValue类型的字段除外
     */
    private boolean isActualArgsCorrect(Field field, Object arg, boolean isDtBsonValueType) {
        if (arg == null) {
            return true;
        }
        if (arg instanceof Enum) {
            return true;
        }
        if (isDtBsonValueType) {
            return field.getType().isInstance(arg);
        }
        if (ClassUtils.isPrimitiveOrWrapper(field.getType())) {
            return ClassUtils.isAssignable(arg.getClass(), field.getType());
        }
        if (field.getType() != GlobalModels.getCGLibProxyRawClass(arg.getClass())) {
            return false;
        }
        return true;
    }

    private Object interceptSetter(Object receiver, Object[] args, MethodProxy methodProxy,
                                   Field field, boolean isImmutableType, boolean isDtBsonValueType, String name) throws Throwable {
        Object arg = args[0];
        boolean argImmutable = isImmutableObject(arg, isImmutableType, isDtBsonValueType);

        if (!isActualArgsCorrect(field, arg, isDtBsonValueType)) {
            throw new IllegalArgumentException("Parameter type and field type are inconsistent");
        }

        DocumentNode receiverDocument = (DocumentNode) receiver;
        //加锁保证多线程对同一文档的同一字段赋值时，字段的最终值和根文档的更新记录是一致的
        //此过程只加锁了receiverDocument，如果receiverDocument的parent在此期间被set/unset，最终
        //记录的是路径更短的parent的更新，receiverDocument的更新变成了无关的，所以不会导致问题
        synchronized (receiverDocument.getRecordLock()) {
            if (!argImmutable) {
                //由于arg可能并发的关联到其他parent，这个关联只能成功一次，所以先关联成功后再调用setter
                ((DocumentNode) arg).setParent(receiverDocument, name);
            }

            Object previous = field.get(receiverDocument);

            //假设setter和getter的实现都是简单的，里面不会去对别的对象加锁，所以不会产生死锁
            Object r = null;
            try {
                r = methodProxy.invokeSuper(receiver, args);
            } catch (Throwable throwable) {
                //setter抛出异常
                if (!argImmutable) {
                    ((DocumentNode) arg).unsetParent(receiverDocument);
                }
                throw throwable;
            }

            //set成功后，取消以前字段值的parent
            boolean previousImmutable = isImmutableObject(previous, isImmutableType, isDtBsonValueType);
            if (!previousImmutable) {
                ((DocumentNode) previous).unsetParent(receiverDocument);
            }

            receiverDocument.recordFieldAssign(arg, previous, name);
            return r;
        }
    }

}
