package com.mountsea.django.example;

import com.mountsea.django.core.CollectibleDocument;
import com.mountsea.django.core.cache.EnableDocumentCache;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EnableDocumentCache(maxSize = 1000, expireAfterAccessMills = -1)
public class PlayerInfo extends CollectibleDocument {

    private String id;
}
