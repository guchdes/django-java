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

import io.github.guchdes.django.bson.projection.ContainerDocumentNode;
import io.github.guchdes.django.bson.projection.DocumentEnumMap;
import io.github.guchdes.django.bson.projection.DocumentClassDefinitionException;
import io.github.guchdes.django.bson.projection.DocumentNode;
import io.github.guchdes.django.bson.util.InternalUtils;
import io.github.guchdes.django.bson.util.LazyInitializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author guch
 * @since 3.0.0
 */
class DocumentNodeFillerProviderImpl implements DocumentNodeFillerProvider {

    private final Map<Class<? extends DocumentNode>, DocumentNodeFiller<?>> DOCUMENT_NODE_FILLER_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends DocumentNode> DocumentNodeFiller<T> getDocumentNodeFiller(Class<T> aClass) {
        return (DocumentNodeFiller<T>) InternalUtils.mapComputeIfAbsent(DOCUMENT_NODE_FILLER_MAP, aClass, c -> {
            return new LazyDocumentNodeFiller<>(() -> (DocumentNodeFiller<T>) create(c));
        });
    }

    @SuppressWarnings("unchecked")
    private DocumentNodeFiller<?> create(Class<? extends DocumentNode> aClass) {
        ClassModel<?> classModel = GlobalModels.getClassModel(GlobalModels.getCGLibProxyRawClass(aClass));
        List<PropertyModel<?>> propertyModels = classModel.getPropertyModels();
        List<Consumer<Object>> propertyProcessors = new ArrayList<>();
        for (PropertyModel<?> propertyModel : propertyModels) {
            Class<?> type = propertyModel.getTypeData().getType();
            boolean immutableType = GlobalModels.isImmutableType(type);
            if (immutableType) {
                continue;
            }
            PropertyAccessor<DocumentNode> propertyAccessor = (PropertyAccessor<DocumentNode>) propertyModel.getPropertyAccessor();
            if (ContainerDocumentNode.class.isAssignableFrom(type)) {
                Supplier<Object> instanceCreator = containerInstanceCreator(propertyModel);
                propertyProcessors.add(o -> {
                    Object p = propertyAccessor.get(o);
                    if (p == null) {
                        propertyAccessor.set(o, (DocumentNode) instanceCreator.get());
                    }
                });
            } else if (DocumentNode.class.isAssignableFrom(type)) {
                ClassModel<?> model1 = GlobalModels.getClassModel(type);
                DocumentNodeFiller<DocumentNode> filler = getDocumentNodeFiller((Class<DocumentNode>) type);
                propertyProcessors.add(o -> {
                    Object p = propertyAccessor.get(o);
                    if (p == null) {
                        DocumentNode instance = (DocumentNode) model1.getInstanceCreator().getInstance();
                        filler.accept(instance);
                        propertyAccessor.set(o, instance);
                    }
                });
            } else {
                throw new DocumentClassDefinitionException("Unknown property type:" + type);
            }
        }
        return o -> {
            for (Consumer<Object> propertyProcessor : propertyProcessors) {
                propertyProcessor.accept(o);
            }
        };
    }

    private static Supplier<Object> containerInstanceCreator(PropertyModel<?> propertyModel) {
        TypeData<?> typeData = propertyModel.getPropertyMetadata().getTypeData();
        Class<?> type = typeData.getType();
        if (EnumMap.class.isAssignableFrom(type) || DocumentEnumMap.class.isAssignableFrom(type)) {
            Type[] mapKeyValueType = TypeWithTypeParametersUtils.getMapKeyValueType(typeData);
            Type keyType = mapKeyValueType[0];
            if (keyType instanceof TypeVariable) {
                throw new IllegalArgumentException("Enum Map no key type.");
            }
            Class<?> rawType = TypeWithTypeParametersUtils.getRawType(keyType);
            try {
                Constructor<?> constructor = type.getConstructor(Class.class);
                return () -> {
                    try {
                        return constructor.newInstance(rawType);
                    } catch (Exception e) {
                        throw new DocumentClassDefinitionException("create enum map", e);
                    }
                };
            } catch (NoSuchMethodException e) {
                throw new DocumentClassDefinitionException("EnumMap or DocumentEnumMap no Enum Constructor:" + type);
            }
        } else {
            ClassModel<?> model1 = GlobalModels.getClassModel(type);
            return () -> model1.getInstanceCreator().getInstance();
        }
    }

    private static class LazyDocumentNodeFiller<T extends DocumentNode> implements DocumentNodeFiller<T> {
        private final LazyInitializer<DocumentNodeFiller<T>> supplier;

        public LazyDocumentNodeFiller(Supplier<DocumentNodeFiller<T>> supplier) {
            this.supplier = new LazyInitializer<>(supplier);
        }

        @Override
        public void accept(T t) {
            supplier.get().accept(t);
        }
    }
}
