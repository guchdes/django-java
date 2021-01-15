package io.github.guchdes.django.core

import com.mongodb.client.model.Projections
import io.github.guchdes.django.core.exception.IllegalIdTypeDjangoException
import io.github.guchdes.django.core.model.MultiEquals
import org.bson.types.ObjectId

import static io.github.guchdes.django.core.CRUDTestClasses.*

/**
 * @Author guch
 */
class GetSpecification extends DaoOperationalSpecification {

    def setup() {
        cleanupSpec()
    }

    def cleanupSpec() {
        allClasses.forEach { dropCollection0(it) }
    }

    def "test getByKey"() {
        when:
        def parse = parseDocument(clazz, keyDocument)
        def key = extractKey(parse)
        def document = dao.findByKey(clazz, key)
        then:
        document == null

        when:
        document = dao.getByKey(clazz, key)
        then:
        documentContains(keyDocument, document)

        where:
        clazz                     | keyDocument
        IntIdDocument             | ['_id': 1]
        ObjectIdIdDocument        | ['_id': new ObjectId()]
        VectorIdDocument          | ['_id': ['x': 100f, 'y': 200f]]
        NameKeyObjectIdIdDocument | ['name': 'k2']
        MKeyClassDocument         | ['base': 'k1', 'key2': ['q': [1, 2]], 'key3': ['x': 1.23d, 'y': 3.21d]]
        SKeyClassDocument         | ['key2': 333]
    }

    def "test getByKey and projection"() {
        when:
        parseAndInsertDocument0(IntIdDocument, ['_id': 1, 'name': '111', 'setting': ['noticeOn': true]])
        def document = dao.getByKey(IntIdDocument, 1, Projections.include('setting'))
        then:
        documentEquals(['_id': 1, 'setting': ['noticeOn': true, 'number': 0]], document)
    }

    def "test getByFields"() {
        when:
        def parse = parseDocument(clazz, keyDocument)
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(clazz).getKeyDefinition()
        def fields = definition.keyExtractor.extractBsonKey(parse, false)
        def document = dao.findOne(clazz, fields)
        then:
        document == null

        when:
        document = dao.getByFields(clazz, parseDocument(MultiEquals, fields))
        then:
        documentContains(keyDocument, document)

        where:
        clazz                     | keyDocument
        IntIdDocument             | ['_id': 1]
        ObjectIdIdDocument        | ['_id': new ObjectId()]
        VectorIdDocument          | ['_id': ['x': 100f, 'y': 200f]]
        NameKeyObjectIdIdDocument | ['name': 'k2']
        MKeyClassDocument         | ['base': 'k1', 'key2': ['q': [1, 2]], 'key3': ['x': 1.23d, 'y': 3.21d]]
        SKeyClassDocument         | ['key2': 333]
    }

    def "test getByFields and projection"() {
        when:
        parseAndInsertDocument0(IntIdDocument, ['_id': 1, 'name': '111', 'setting': ['noticeOn': true]])
        def document = dao.getByFields(IntIdDocument, MultiEquals.withId(1), Projections.include('setting'))
        then:
        documentEquals(['_id': 1, 'setting': ['noticeOn': true, 'number': 0]], document)
    }

    def "should getByKey throws when not get by id and id type is not ObjectId"() {
        when:
        dao.getByKey(NameKeyIntIdDocument, "aaa")
        then:
        thrown(IllegalIdTypeDjangoException)
    }

    def "should getByFields throws when not get by id and id type is not ObjectId"() {
        when:
        dao.getByFields(NameKeyIntIdDocument, MultiEquals.with("name", "aaa"))
        then:
        thrown(IllegalIdTypeDjangoException)
    }

    def "should getByKey auto gen id when document not exists and not get by id"() {
        when:
        def a1 = dao.getByKey(NameKeyObjectIdIdDocument, "aaa")
        then:
        a1.getId() != null

        when:
        def a2 = dao.getByKey(NameKeyObjectIdIdDocument, "aaa")
        then:
        a1.getId() == a2.getId()
    }

    def "should getByFields auto gen id when document not exists and not get by id"() {
        when:
        def a1 = dao.getByFields(NameKeyObjectIdIdDocument, MultiEquals.with("name", "aaa"))
        then:
        a1.getId() != null

        when:
        def a2 = dao.getByFields(NameKeyObjectIdIdDocument, MultiEquals.with("name", "aaa"))
        then:
        a1.getId() == a2.getId()
    }

}
