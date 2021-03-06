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
package com.mountsea.django.core;

import com.mountsea.django.core.CollectibleDocumentsSpecification;
import com.mountsea.django.core.annotation.KeyClass;
import com.mountsea.django.core.annotation.KeyField;
import com.mountsea.django.core.annotation.KeyFieldName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * @author guch
 * @since 3.0.0
 * @see CollectibleDocumentsSpecification
 */
public class CollectibleDocumentTestClasses {

    @Getter
    @Setter
    public static abstract class Base extends CollectibleDocument {
        private Integer id;
        @KeyField
        private String base;
    }

    @Getter
    @Setter
    public static abstract class Base2 extends Base {
        //覆盖Base1中的@KeyField注解
        @KeyField
        private String base2;
    }

    @Getter
    @Setter
    @KeyFieldName("base")
    public static class FieldNameAnno extends Base2 {
    }

    @Getter
    @Setter
    @KeyFieldName("baseAA")
    public static class WrongFieldNameAnno extends Base2 {
    }

    @Getter
    @Setter
    @KeyFieldName("id")
    public static class KeyFieldNameIdAnno extends Base2 {
    }

    @Getter
    @Setter
    @KeyClass(Key.class)
    public static class KeyClassAnno extends Base2 {
        private int f1;
        private String f2;
    }

    @Getter
    @Setter
    @KeyClass(WrongFieldNameKey.class)
    public static class WrongFieldNameKeyClassAnno extends Base2 {
        private int f1;
        private String f2;
    }

    @Getter
    @Setter
    @KeyClass(WrongFieldTypeKey.class)
    public static class WrongFieldTypeClassAnno extends Base2 {
        private int f1;
        private String f2;
    }

    @Data
    public static class Key {
        private int f1;
        private String f2;
        private String base;
    }

    @Data
    public static class WrongFieldNameKey {
        private int f1;
        private int baseAA;
    }

    @Data
    public static class WrongFieldTypeKey {
        private int f1;
        private long base;
    }

    @Getter
    @Setter
    public static class UseSuperClassAnno extends Base2 {
    }

    @Getter
    @Setter
    public static class UseDefaultId extends CollectibleDocument {
        private Long id;
    }

    @Getter
    @Setter
    public static class KeyFieldAnno extends Base2 {
        @KeyField
        private long ff;
    }

    @Data
    public static class Point {
        @BsonCreator
        public Point(@BsonProperty("x") int x, @BsonProperty("y") int y) {
            this.x = x;
            this.y = y;
        }

        private final int x;
        private final int y;
    }

    @Getter
    @Setter
    public static class StructKeyFieldAnno extends Base2 {
        @KeyField
        private Point point;
    }

}
