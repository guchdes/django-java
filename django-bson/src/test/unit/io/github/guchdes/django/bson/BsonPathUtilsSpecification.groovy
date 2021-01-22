package io.github.guchdes.django.bson


import org.bson.BsonDocument
import spock.lang.Specification

/**
 * @author guch
 * @since 3.0.0
 */
class BsonPathUtilsSpecification extends Specification {

    def "should correctly get path element"() {
        given:
        BsonDocument bsonDocument = BsonUtils.toBsonDocument(['a': ['aa': [["aaa": 1], ["bbb": 2]]]])
        expect:
        BsonPathUtils.getPathElement(bsonDocument, "a.aa.0.aaa").asInt32().value == 1
        BsonPathUtils.getPathElement(bsonDocument, "a.aa.1.bbb").asInt32().value == 2
        BsonPathUtils.getPathElement(bsonDocument, "a.bb.0.ccc") == null
        BsonPathUtils.getPathElement(bsonDocument, "a.bb.1.ccc") == null
    }

}
