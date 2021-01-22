package io.github.guchdes.django.example;

import io.github.guchdes.django.bson.annotation.FillNullByEmpty;
import io.github.guchdes.django.bson.projection.DocumentNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FillNullByEmpty
public class Sheath extends DocumentNode {
    private Equip equip;
}
