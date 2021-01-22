package io.github.guchdes.django.example;

import io.github.guchdes.django.core.CollectibleDocument;
import io.github.guchdes.django.core.cache.EnableDocumentCache;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EnableDocumentCache(maxSize = 1000, expireAfterAccessMills = -1)
public class PlayerInfo extends CollectibleDocument {

    private String id;
}
