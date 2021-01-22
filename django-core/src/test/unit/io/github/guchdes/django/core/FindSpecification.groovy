package io.github.guchdes.django.core

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import io.github.guchdes.django.core.model.MultiEquals
import org.bson.types.ObjectId

import static io.github.guchdes.django.core.CRUDTestClasses.*;

/**
 * @author guch
 */
class FindSpecification extends DaoOperationalSpecification {

    static base = ['base' : 'base', 'setting': ['noticeOn': true, 'number': 666],
                   'items': [1: ['itemId': 1, 'date': new Date(), 'tags': ['a', 's', 'd']]]]

    def setup() {
        cleanupSpec()
    }

    def cleanupSpec() {
        allClasses.forEach { dropCollection0(it) }
    }

    def "test findByKey"() {
        when:
        def parse = parseDocument(clazz, expectDocument)
        def key = extractKey(parse)
        insertDocument0(clazz, parse)
        def document = DaoOperationalSpecification.dao.findByKey(clazz, key)
        then:
        documentEquals(expectDocument, document)

        where:
        clazz                     | expectDocument
        IntIdDocument             | ['_id': 1, 'name': 'ss'] + base
        ObjectIdIdDocument        | ['_id': new ObjectId(), 'name': 'ss'] + base
        VectorIdDocument          | ['_id': ['x': 100f, 'y': 200f], 'name': 'ss'] + base
        NameKeyIntIdDocument      | ['_id': 111, 'name': 'k1'] + base
        NameKeyObjectIdIdDocument | ['_id': new ObjectId(), 'name': 'k2'] + base
        MKeyClassDocument         | ['_id': new ObjectId(), 'base': 'k1', 'key2': ['q': [1, 2]], 'key3': ['x': 1.23d, 'y': 3.21d]] + base
        SKeyClassDocument         | ['_id': new ObjectId(), 'key2': 333] + base
    }

    def "test findByKey and projection"() {
        when:
        parseAndInsertDocument0(IntIdDocument, ['_id': 1, 'name': 'ss'] + base)
        def document = DaoOperationalSpecification.dao.findByKey(IntIdDocument, 1, Projections.include("setting"))
        then:
        documentEquals(['setting': base.get('setting'), '_id': 1], document)
    }

    def "test findOne"() {
        when:
        def parse = parseDocument(clazz, expectDocument)
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(clazz).getKeyDefinition()
        def bsonKey = definition.keyExtractor.extractBsonKey(parse, false)
        insertDocument0(clazz, parse)
        def document = DaoOperationalSpecification.dao.findOne(clazz, bsonKey)
        then:
        documentEquals(expectDocument, document)

        where:
        clazz                     | expectDocument
        IntIdDocument             | ['_id': 1, 'name': 'ss'] + base
        ObjectIdIdDocument        | ['_id': new ObjectId(), 'name': 'ss'] + base
        VectorIdDocument          | ['_id': ['x': 100f, 'y': 200f], 'name': 'ss'] + base
        NameKeyIntIdDocument      | ['_id': 111, 'name': 'k1'] + base
        NameKeyObjectIdIdDocument | ['_id': new ObjectId(), 'name': 'k2'] + base
        MKeyClassDocument         | ['_id': new ObjectId(), 'base': 'k1', 'key2': ['q': [1, 2]], 'key3': ['x': 1.23d, 'y': 3.21d]] + base
        SKeyClassDocument         | ['_id': new ObjectId(), 'key2': 333] + base
    }

    def "test findOne and projection"() {
        when:
        parseAndInsertDocument0(IntIdDocument, ['_id': 1, 'name': 'ss'] + base)
        def document = DaoOperationalSpecification.dao.findOne(IntIdDocument, MultiEquals.withId(1), Projections.include("setting"))
        then:
        documentEquals(['setting': base.get('setting'), '_id': 1], document)
    }

    def "test findAll"() {
        given:
        def d1 = parseDocument(IntIdDocument, base + ['_id': 1, 'setting': ['noticeOn': true, 'number': 666]])
        def d2 = parseDocument(IntIdDocument, base + ['_id': 2, 'setting': ['noticeOn': true, 'number': 666]])
        def d3 = parseDocument(IntIdDocument, base + ['_id': 3, 'setting': ['noticeOn': false, 'number': 666]])
        insertDocument0(IntIdDocument, d1)
        insertDocument0(IntIdDocument, d2)
        insertDocument0(IntIdDocument, d3)

        when:
        def list = DaoOperationalSpecification.dao.findAll(IntIdDocument, Filters.eq("setting.noticeOn", true))
        then:
        documentSetEquals([d1, d2], list)

        when:
        list = DaoOperationalSpecification.dao.findAll(IntIdDocument, Filters.eq("setting.noticeOn", true), Projections.include("base"))
        then:
        documentSetEquals([['_id': 1, 'base': 'base'], ['_id': 2, 'base': 'base']], list)

        when:
        list = DaoOperationalSpecification.dao.findAll(IntIdDocument, Filters.eq("setting.noticeOn", true), null, Sorts.descending("_id"))
        then:
        documentListEquals([d2, d1], list)

        when:
        list = DaoOperationalSpecification.dao.findAll(IntIdDocument, Filters.eq("setting.noticeOn", true), null,
                1, 1, Sorts.descending("_id"))
        then:
        documentListEquals([d1], list)
    }


}
