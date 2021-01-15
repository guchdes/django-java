package io.github.guchdes.django.core

import com.mongodb.MongoException
import io.github.guchdes.django.bson.projection.DocumentNodeHelper
import io.github.guchdes.django.core.exception.IllegalIdTypeDjangoException
import io.github.guchdes.django.core.model.MultiEquals
import io.github.guchdes.django.core.model.SaveMode
import org.bson.types.ObjectId
import static io.github.guchdes.django.core.CRUDTestClasses.*

/**
 * @Author guch
 */
class SaveSpecification extends DaoOperationalSpecification {
    static base = ['base' : 'base', 'setting': ['noticeOn': true, 'number': 666],
                   'items': [1: ['itemId': 1, 'date': new Date(), 'tags': ['a', 's', 'd']]]]

    static allCases = [[IntIdDocument, ['_id': 1, 'name': 'ss'] + base],
                       [ObjectIdIdDocument, ['_id': new ObjectId(), 'name': 'ss'] + base],
                       [VectorIdDocument, ['_id': ['x': 100f, 'y': 200f], 'name': 'ss'] + base],
                       [NameKeyIntIdDocument, ['_id': 111, 'name': 'k1'] + base],
                       [NameKeyObjectIdIdDocument, ['_id': new ObjectId(), 'name': 'k2'] + base],
                       [MKeyClassDocument, ['_id': new ObjectId(), 'base': 'k1', 'key2': ['q': [1, 2]], 'key3': ['x': 1.23d, 'y': 3.21d]] + base],
                       [SKeyClassDocument, ['_id': new ObjectId(), 'key2': 333] + base]]

    def setup() {
        cleanupSpec()
    }

    def cleanupSpec() {
        allClasses.forEach { dropCollection0(it) }
    }

    def "test saveByKey basics"() {
        given:
        def document = parseDocument(clazz, saveDocument)
        def keyDefinition = CollectibleDocumentDefinitions.getDocumentDefinition(clazz).getKeyDefinition()
        def key = keyDefinition.keyExtractor.extractBsonKey(document, false)

        when: 'insert new with INSERT_ONLY'
        def saveResult = dao.saveByKey(document, SaveMode.INSERT_ONLY)
        then:
        saveResult.inserted
        !saveResult.updated
        documentEquals(document, findDocument0(clazz, key))

        when: 'insert existing document with INSERT_ONLY'
        dao.saveByKey(document, SaveMode.INSERT_ONLY)
        then:
        thrown(MongoException)
        !DocumentNodeHelper.hasEnableUpdateCollect(document)

        when: 'update existing documents with UPDATE_ONLY'
        DocumentNodeHelper.enableUpdateRecord(document)
        document.setting.note = 'hello'
        document.setting.number = 666
        document.items[1].tags.add('f')
        saveResult = dao.saveByKey(document, SaveMode.UPDATE_ONLY)
        then:
        !saveResult.inserted
        saveResult.updated
        documentEquals(document, findDocument0(clazz, key))
        DocumentNodeHelper.hasEnableUpdateCollect(document)

        when: 'update existing documents with INSERT_OR_UPDATE'
        document.setting.note = null
        document.items[1].tags.add('g')
        saveResult = dao.saveByKey(document, SaveMode.INSERT_OR_UPDATE)
        then:
        !saveResult.inserted
        saveResult.updated
        documentEquals(document, findDocument0(clazz, key))
        DocumentNodeHelper.hasEnableUpdateCollect(document)

        when: 'update not existing documents with UPDATE_ONLY'
        dropCollection0(clazz)
        document.setting.note = 'bbb'
        document.items[1].tags.add('w')
        saveResult = dao.saveByKey(document, SaveMode.UPDATE_ONLY)
        then:
        !saveResult.inserted
        !saveResult.updated
        findDocument0(clazz, key) == null
        !DocumentNodeHelper.hasEnableUpdateCollect(document)

        when: 'insert not existing documents with INSERT_OR_UPDATE'
        document.setting.note = 'aaa'
        document.items[1].tags.add('w')
        saveResult = dao.saveByKey(document, SaveMode.INSERT_OR_UPDATE)
        then:
        saveResult.inserted
        !saveResult.updated
        documentEquals(document, findDocument0(clazz, key))
        DocumentNodeHelper.hasEnableUpdateCollect(document)

        where:
        clazz                     | saveDocument
        IntIdDocument             | ['_id': 1, 'name': 'ss'] + base
        ObjectIdIdDocument        | ['_id': new ObjectId(), 'name': 'ss'] + base
        VectorIdDocument          | ['_id': ['x': 100f, 'y': 200f], 'name': 'ss'] + base
        NameKeyIntIdDocument      | ['_id': 111, 'name': 'k1'] + base
        NameKeyObjectIdIdDocument | ['_id': new ObjectId(), 'name': 'k2'] + base
        MKeyClassDocument         | ['_id': new ObjectId(), 'base': 'k1', 'key2': ['q': [1, 2]], 'key3': ['x': 1.23d, 'y': 3.21d]] + base
        SKeyClassDocument         | ['_id': new ObjectId(), 'key2': 333] + base
    }

    def "test saveByFields basics"() {
        given:
        def document = parseDocument(clazz, saveDocument)
        def keyDefinition = CollectibleDocumentDefinitions.getDocumentDefinition(clazz).getKeyDefinition()
        def key = keyDefinition.keyExtractor.extractBsonKey(document, false)
        def fields = new ArrayList(fieldsMap.keySet())

        when: 'insert new with INSERT_ONLY'
        def saveResult = dao.saveByFields(document, SaveMode.INSERT_ONLY, fields)
        then:
        saveResult.inserted
        !saveResult.updated
        documentEquals(document, findDocument0(clazz, key))

        when: 'insert existing document with INSERT_ONLY'
        dao.saveByFields(document, SaveMode.INSERT_ONLY, fields)
        then:
        thrown(MongoException)
        !DocumentNodeHelper.hasEnableUpdateCollect(document)

        when: 'update existing documents with UPDATE_ONLY'
        DocumentNodeHelper.enableUpdateRecord(document)
        document.setting.note = 'hello'
        document.setting.number = 666
        document.items[1].tags.add('f')
        saveResult = dao.saveByFields(document, SaveMode.UPDATE_ONLY, fields)
        then:
        !saveResult.inserted
        saveResult.updated
        documentEquals(document, findDocument0(clazz, key))
        DocumentNodeHelper.hasEnableUpdateCollect(document)

        when: 'update existing documents with INSERT_OR_UPDATE'
        document.setting.note = null
        document.items[1].tags.add('g')
        saveResult = dao.saveByFields(document, SaveMode.INSERT_OR_UPDATE, fields)
        then:
        !saveResult.inserted
        saveResult.updated
        documentEquals(document, findDocument0(clazz, key))
        DocumentNodeHelper.hasEnableUpdateCollect(document)

        when: 'update not existing documents with UPDATE_ONLY'
        dropCollection0(clazz)
        document.setting.note = 'bbb'
        document.items[1].tags.add('w')
        saveResult = dao.saveByFields(document, SaveMode.UPDATE_ONLY, fields)
        then:
        !saveResult.inserted
        !saveResult.updated
        findDocument0(clazz, key) == null
        !DocumentNodeHelper.hasEnableUpdateCollect(document)

        when: 'insert not existing documents with INSERT_OR_UPDATE'
        document.setting.note = 'aaa'
        document.items[1].tags.add('w')
        saveResult = dao.saveByFields(document, SaveMode.INSERT_OR_UPDATE, fields)
        then:
        saveResult.inserted
        !saveResult.updated
        documentEquals(document, findDocument0(clazz, key))
        DocumentNodeHelper.hasEnableUpdateCollect(document)

        where:
        [clazz, saveDocument, fieldsMap] << [
                [[IntIdDocument, ['_id': 1, 'name': 'ss'] + base],
                 [ObjectIdIdDocument, ['_id': new ObjectId(), 'name': 'ss'] + base],
                 [VectorIdDocument, ['_id': ['x': 100f, 'y': 200f], 'name': 'ss'] + base],
                 [NameKeyIntIdDocument, ['_id': 111, 'name': 'k1'] + base],
                 [NameKeyObjectIdIdDocument, ['_id': new ObjectId(), 'name': 'k2'] + base],
                 [MKeyClassDocument, ['_id': new ObjectId(), 'name': 'ss', 'key2': ['q': [1, 2]], 'key3': ['x': 1.23d, 'y': 3.21d]] + base],
                 [SKeyClassDocument, ['_id': new ObjectId(), 'name': 'ss', 'key2': 333] + base]],
                [['id': 'id'], ['name': 'name', 'base': 'base']]].combinations()*.flatten()
    }

    def "should throws when document no id and id type is not ObjectId"() {
        when:
        NameKeyIntIdDocument document = parseDocument(NameKeyIntIdDocument, ['name': 'k1'] + base)
        if (byKey) {
            dao.saveByKey(document, saveMode)
        } else {
            dao.saveByFields(document, saveMode, ['name'])
        }
        then:
        thrown(IllegalIdTypeDjangoException)

        where:
        [saveMode, byKey] << [[SaveMode.INSERT_ONLY, SaveMode.INSERT_OR_UPDATE], [true, false]].combinations()
    }

    def "should saveByFields throws when fields contains id and fields.size() > 1"() {
        when:
        NameKeyIntIdDocument document = parseDocument(NameKeyIntIdDocument, ['name': 'k1'] + base)
        dao.saveByFields(document, SaveMode.INSERT_ONLY, ['id', 'name'])
        then:
        thrown(IllegalArgumentException)
    }

    def "should auto gen id when saving"() {
        when:
        NameKeyObjectIdIdDocument document = parseDocument(NameKeyObjectIdIdDocument, ['name': 'k1'] + base)
        if (byKey) {
            dao.saveByKey(document, saveMode)
        } else {
            dao.saveByFields(document, saveMode, ['name'])
        }
        then:
        document.getId() != null
        findDocument0(NameKeyObjectIdIdDocument, MultiEquals.with("name", "k1")).getId() == document.getId()

        where:
        [saveMode, byKey] << [[SaveMode.INSERT_ONLY, SaveMode.INSERT_OR_UPDATE], [true, false]].combinations()
    }
//
//    def "should disable update collect when save throws any exception"() {
//        given:
//        NameKeyObjectIdIdDocument document = parseDocument(NameKeyObjectIdIdDocument, ['name': 'k1'] + base)
//
//    }
}
