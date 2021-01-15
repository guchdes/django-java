package io.github.guchdes.django.bson.projection

import io.github.guchdes.django.bson.BsonUtils
import org.bson.BsonDocument
import org.bson.BsonInt32

/**
 * @Author guch
 * @Since 3.0.0
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
