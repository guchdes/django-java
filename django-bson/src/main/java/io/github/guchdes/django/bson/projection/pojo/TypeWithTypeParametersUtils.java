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

import javax.annotation.Nullable;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class TypeWithTypeParametersUtils {
    @Nullable
    public static Class<?> getRawType(Type type) {
        if (type == null) {
            return null;
        }
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
            if (!(type instanceof Class)) {
                throw new IllegalArgumentException();
            }
            return (Class<?>) type;
        } else {
            return null;
        }
    }

    /**
     * 推导Map的KV参数，由于多层继承中可能写死K或V的类型，所以typeWithTypeParameters未必有两个参数对应KV，
     * 需要进行推导。
     */
    static Type[] getMapKeyValueType(TypeWithTypeParameters<?> typeWithTypeParameters) {
        Type genericType = toGenericType(typeWithTypeParameters);
        TypeVariable<?> keyVar = Map.class.getTypeParameters()[0];
        TypeVariable<?> valueVar = Map.class.getTypeParameters()[1];
        Type keyType = org.apache.commons.lang3.reflect.TypeUtils.getTypeArguments(genericType, Map.class).get(keyVar);
        Type valueType = org.apache.commons.lang3.reflect.TypeUtils.getTypeArguments(genericType, Map.class).get(valueVar);
        return new Type[]{keyType, valueType};
    }

    static Type toGenericType(TypeWithTypeParameters<?> typeWithTypeParameters) {
        if (typeWithTypeParameters.getTypeParameters().isEmpty()) {
            return typeWithTypeParameters.getType();
        }
        Type[] args = new Type[typeWithTypeParameters.getTypeParameters().size()];
        for (int i = 0; i < typeWithTypeParameters.getTypeParameters().size(); i++) {
            args[i] = toGenericType(typeWithTypeParameters.getTypeParameters().get(i));
        }
        return new ParameterizedTypeImpl(typeWithTypeParameters.getType(), args, null);
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {
        private final Type[] actualTypeArguments;
        private final Class<?> rawType;
        private final Type ownerType;

        private ParameterizedTypeImpl(Class<?> var1, Type[] var2, Type var3) {
            this.actualTypeArguments = var2;
            this.rawType = var1;
            this.ownerType = (var3 != null ? var3 : var1.getDeclaringClass());
            this.validateConstructorArguments();
        }

        private void validateConstructorArguments() {
            TypeVariable<?>[] var1 = this.rawType.getTypeParameters();
            if (var1.length != this.actualTypeArguments.length) {
                throw new MalformedParameterizedTypeException();
            } else {
                for (int var2 = 0; var2 < this.actualTypeArguments.length; ++var2) {
                }

            }
        }

        public static ParameterizedTypeImpl make(Class<?> var0, Type[] var1, Type var2) {
            return new ParameterizedTypeImpl(var0, var1, var2);
        }

        public Type[] getActualTypeArguments() {
            return this.actualTypeArguments.clone();
        }

        public Class<?> getRawType() {
            return this.rawType;
        }

        public Type getOwnerType() {
            return this.ownerType;
        }

        public boolean equals(Object var1) {
            if (var1 instanceof ParameterizedType) {
                ParameterizedType var2 = (ParameterizedType) var1;
                if (this == var2) {
                    return true;
                } else {
                    Type var3 = var2.getOwnerType();
                    Type var4 = var2.getRawType();
                    return Objects.equals(this.ownerType, var3) && Objects.equals(this.rawType, var4) && Arrays.equals(this.actualTypeArguments, var2.getActualTypeArguments());
                }
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
        }

        public String toString() {
            StringBuilder var1 = new StringBuilder();
            if (this.ownerType != null) {
                if (this.ownerType instanceof Class) {
                    var1.append(((Class<?>) this.ownerType).getName());
                } else {
                    var1.append(this.ownerType.toString());
                }

                var1.append("$");
                if (this.ownerType instanceof ParameterizedTypeImpl) {
                    var1.append(this.rawType.getName().replace(((ParameterizedTypeImpl) this.ownerType).rawType.getName() + "$", ""));
                } else {
                    var1.append(this.rawType.getSimpleName());
                }
            } else {
                var1.append(this.rawType.getName());
            }

            if (this.actualTypeArguments != null && this.actualTypeArguments.length > 0) {
                var1.append("<");
                boolean var2 = true;
                Type[] var3 = this.actualTypeArguments;
                int var4 = var3.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    Type var6 = var3[var5];
                    if (!var2) {
                        var1.append(", ");
                    }

                    var1.append(var6.getTypeName());
                    var2 = false;
                }

                var1.append(">");
            }

            return var1.toString();
        }
    }

}
