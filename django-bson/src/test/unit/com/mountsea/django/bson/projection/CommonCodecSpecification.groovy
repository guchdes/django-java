package com.mountsea.django.bson.projection

import com.mountsea.django.bson.BsonConvertingSpecification
import com.mountsea.django.bson.Level
import org.bson.BsonInt32

/**
 * @author guch
 * @since 3.0.0
 */
class CommonCodecSpecification extends BsonConvertingSpecification {

    def "test non property Enum coding"() {
        when:
        Map<Integer, Level> map = new HashMap<>()
        map.put(1, Level.L1)
        def bsonDocument = toBsonDocument(map)
        then:
        bsonDocument.get("1") == new BsonInt32(1)
    }
}
