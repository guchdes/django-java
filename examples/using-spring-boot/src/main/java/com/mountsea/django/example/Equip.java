package io.github.guchdes.django.example;

import io.github.guchdes.django.bson.projection.DocumentNode;
import lombok.Getter;
import lombok.Setter;

/**
 * 装备文档类
 */
@Getter
@Setter
public class Equip extends DocumentNode {
    public static Equip create() {
        return create(Equip.class);
    }

    protected Equip() {
    }

    private int equipId;

    private int level;

    private String name;
}
