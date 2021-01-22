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
package io.github.guchdes.django.core;

import io.github.guchdes.django.bson.projection.DocumentList;
import io.github.guchdes.django.bson.projection.DocumentMap;
import io.github.guchdes.django.bson.projection.DocumentNode;
import io.github.guchdes.django.core.annotation.KeyClass;
import io.github.guchdes.django.core.annotation.KeyField;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author guch
 * @since 3.0.0
 */
public class CRUDTestClasses {
    public static final List<Class<?>> allClasses = Arrays.asList(IntIdDocument.class,
            ObjectIdIdDocument.class, VectorIdDocument.class, NameKeyIntIdDocument.class,
            NameKeyObjectIdIdDocument.class, MKeyClassDocument.class, SKeyClassDocument.class);

    @Getter
    @Setter
    @ToString
    public static class SettingDocument extends DocumentNode {
        private boolean noticeOn;
        private int number;
        private String note;
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    public static class FavoriteItemDocument extends DocumentNode {
        private int itemId;

        private Date date;

        private DocumentList<String> tags;
    }

    @Getter
    @Setter
    public abstract static class Base extends CollectibleDocument {
        private String base;

        private SettingDocument setting;

        private DocumentMap<Integer, FavoriteItemDocument> items;
    }

    @Getter
    @Setter
    public static class IntIdDocument extends Base {
        private Integer id;

        private String name;
    }

    @Getter
    @Setter
    public static class ObjectIdIdDocument extends Base {
        private ObjectId id;

        private String name;
    }

    @Data
    public static class Vector {
        private final double x;

        private final double y;

        @BsonCreator
        public Vector(@BsonProperty("x") double x, @BsonProperty("y") double y) {
            this.x = x;
            this.y = y;
        }
    }

    @Getter
    @Setter
    public static class VectorIdDocument extends Base {
        private Vector id;

        private String name;
    }

    @Getter
    @Setter
    public static class NameKeyIntIdDocument extends Base {
        private Integer id;

        @KeyField
        private String name;
    }

    @Getter
    @Setter
    public static class NameKeyObjectIdIdDocument extends Base {
        private ObjectId id;

        @KeyField
        private String name;
    }

    @Getter
    @Setter
    @KeyClass(MKey.class)
    public static class MKeyClassDocument extends Base {
        private ObjectId id;

        private DocumentMap<String, DocumentList<Integer>> key2;

        private Vector key3;

        private String data;

        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MKey {
        private String base;

        private DocumentMap<String, DocumentList<Integer>> key2;

        private Vector key3;
    }

    @Getter
    @Setter
    @KeyClass(SKey.class)
    public static class SKeyClassDocument extends Base {
        private ObjectId id;

        private int key2;

        private String data;

        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SKey {
        private int key2;
    }

}
