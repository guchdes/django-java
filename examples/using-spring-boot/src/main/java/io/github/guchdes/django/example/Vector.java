package io.github.guchdes.django.example;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * 不可变类型
 */
@Data
public class Vector {

    private final int x;
    private final int y;

    @BsonCreator
    public Vector(@BsonProperty("x") int x, @BsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }
}
