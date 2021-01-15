package io.github.guchdes.django.core

import io.github.guchdes.django.bson.BsonUtils
import io.github.guchdes.django.bson.projection.DocumentNodeHelper
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import spock.lang.Specification

class DaoOperationalSpecification extends Specification {

    static CodecRegistry codecRegistry = AutoEnableUCCodecRegistry.DEFAULT_INSTANCE;

    static DjangoDaoFactory daoFactory = Fixture.getDjangoDaoFactory()

    static DatabaseDao dao = daoFactory.createDao()

    static toBsonDocument(Object o) {
        BsonUtils.toBsonDocument(o, codecRegistry)
    }

    static fromBsonDocument(Object bsonDocument, Class aClass) {
        BsonUtils.fromBsonDocument(bsonDocument, aClass, codecRegistry)
    }

    static parseDocument(Class aClass, Object o) {
        def bsonDocument = toBsonDocument(o)
        def doc = fromBsonDocument(bsonDocument, aClass)
        if (doc instanceof CollectibleDocument) {
            DocumentNodeHelper.disableUpdateRecord(doc)
        }
        doc
    }

    static insertDocument0(Class aClass, CollectibleDocument document) {
        def collection = dao.getMongoCollection(aClass)
        collection.insertOne(document)
    }

    static findDocument0(Class aClass, Bson filter) {
        def collection = dao.getMongoCollection(aClass)
        collection.find(filter).first()
    }

    static parseAndInsertDocument0(Class aClass, Object o) {
        def collection = dao.getMongoCollection(aClass)
        collection.insertOne(parseDocument(aClass, o))
    }

    static extractKey(CollectibleDocument document) {
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(document.getClass()).getKeyDefinition()
        definition.keyExtractor.extractKey(document, false)
    }

    static dropCollection0(Class aClass) {
        def collection = dao.getMongoCollection(aClass)
        collection.drop()
    }

    static printIfNotEquals(Object expect, Object actual) {
        if (expect == actual) {
            true
        } else {
            println "expect:" + expect
            println "actual:" + actual
            false
        }
    }

    static documentEquals(Object expect, Object actual) {
        def document1 = toBsonDocument(expect)
        def document2 = toBsonDocument(actual)
        printIfNotEquals(document1, document2)
    }

    static documentContains(Object expect, Object actual) {
        def document1 = toBsonDocument(expect)
        def document2 = toBsonDocument(actual)
        document2.keySet().retainAll(document1.keySet())
        if (document1 == document2) {
            true
        } else {
            println "document not contains all expect entry, document: $document2, expect: $document1"
            false
        }
    }

    static documentSetEquals(Object expect, Object actual) {
        Set set1 = expect.collect(new HashSet<>(), this.&toBsonDocument)
        Set set2 = actual.collect(new HashSet<>(), this.&toBsonDocument)
        printIfNotEquals(set1, set2)
    }

    static documentListEquals(Object expect, Object actual) {
        List list1 = expect.collect(this.&toBsonDocument)
        List list2 = actual.collect(this.&toBsonDocument)
        printIfNotEquals(list1, list2)
    }


}
