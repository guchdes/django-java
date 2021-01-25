package com.mountsea.django.example;

import com.mountsea.django.bson.annotation.FillNullByEmpty;
import com.mountsea.django.bson.projection.DocumentNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FillNullByEmpty
public class Sheath extends DocumentNode {
    private Equip equip;
}

