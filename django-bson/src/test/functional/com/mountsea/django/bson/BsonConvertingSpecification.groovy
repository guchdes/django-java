package com.mountsea.django.bson


import com.mountsea.django.bson.projection.DocumentNode
import com.mountsea.django.bson.projection.MongoUpdateCollectorImpl
import org.bson.BsonDocument
import spock.lang.Specification

/**
 * @author guch
 * @since 3.0.0
 */
class BsonConvertingSpecification extends Specification {

    BsonDocument toBsonDocument(Object o) {
        BsonUtils.toBsonDocument(o)
    }

    def fromBsonDocument(Object o, Class aClass) {
        BsonUtils.fromBsonDocument(o, aClass)
    }

    boolean updateEqualsTo(DocumentNode documentNode, Object expect) {
        def u = toBsonDocument(documentNode.getUpdateRecord(false))
        def e = toBsonDocument(expect)
        if (u == e) {
            true
        } else {
            println "document:$u"
            println "expect:$e"
            false
        }
    }

    boolean updateEqualsTo(MongoUpdateCollectorImpl collector, Object expect) {
        def u = toBsonDocument(collector.getUpdate())
        def e = toBsonDocument(expect)
        if (u == e) {
            true
        } else {
            println "collector:$u"
            println "expect:$e"
            false
        }
    }

    def parseDocumentNode(Object expect, Class<?> type) {
        BsonUtils.fromBsonDocument(toBsonDocument(expect), type)
    }

    DataDocument parseDataDocument(Object expect) {
        parseDocumentNode(expect, DataDocument.class)
    }

    MainDocument parseMainDocument(Object expect) {
        parseDocumentNode(expect, MainDocument.class)
    }

}
