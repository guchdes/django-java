package io.github.guchdes.django.example;

import io.github.guchdes.django.bson.projection.DocumentMap;
import io.github.guchdes.django.core.CollectibleDocument;
import lombok.Getter;
import lombok.Setter;

/**
 * 背包文档类，继承了CollectibleDocument，表示Bag类对应于一个mongodb的collection，默认collection的名字和类名相同
 */
@Getter
@Setter
public class Bag extends CollectibleDocument {
    public static Bag create() {
        return create(Bag.class);
    }

    protected Bag() {
    }

    private String id;

    private DocumentMap<Integer, Equip> equips;
}
