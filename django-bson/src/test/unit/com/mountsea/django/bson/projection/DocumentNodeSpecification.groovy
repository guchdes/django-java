package com.mountsea.django.bson.projection

import com.mountsea.django.bson.BsonConvertingSpecification
import com.mountsea.django.bson.ChainSetterDocument
import com.mountsea.django.bson.EnumTestDocument
import com.mountsea.django.bson.Level
import com.mountsea.django.bson.Sex
import org.bson.BsonInt32
import org.bson.BsonString


class DocumentNodeSpecification extends BsonConvertingSpecification {

    def "should proxied method return the value as it originally"() {
        when:
        def document = ChainSetterDocument.create()
        then:
        document.setF1("asd").setF2(1) == document
    }

    def "test indexed enum"() {
        when:
        def document = EnumTestDocument.create()
        document.setLevel(Level.L1)
        def bson = toBsonDocument(document)
        then:
        bson.get("level") == new BsonInt32(1)

        when:
        def doc = fromBsonDocument(bson, EnumTestDocument)
        then:
        doc.level == Level.L1
    }

    def "test normal enum"() {
        when:
        def document = EnumTestDocument.create()
        document.setSex(Sex.MAN)
        def bson = toBsonDocument(document)
        then:
        bson.get("sex") == new BsonString("MAN")

        when:
        def doc = fromBsonDocument(bson, EnumTestDocument)
        then:
        doc.sex == Sex.MAN
    }

}
