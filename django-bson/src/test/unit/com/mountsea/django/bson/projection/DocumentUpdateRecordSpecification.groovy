package com.mountsea.django.bson.projection

import com.mountsea.django.bson.BsonConvertingSpecification
import com.mountsea.django.bson.MainDocument
import com.mountsea.django.bson.projection.dtbson.DtBsonArray
import com.mountsea.django.bson.projection.dtbson.DtBsonDocument
import com.mountsea.django.bson.projection.dtbson.DtBsonInt32
import com.mountsea.django.bson.projection.dtbson.DtBsonInt64
import com.mountsea.django.bson.projection.dtbson.DtBsonNull
import com.mountsea.django.bson.projection.dtbson.DtBsonString

/**
 * @author guch* @since 3.0.0
 */
class DocumentUpdateRecordSpecification extends BsonConvertingSpecification {

    def "test basic field update recording"() {
        given:
        def document = MainDocument.create()
        document.enableUpdateCollect()

        when:
        document.setId(1)
        document.setName("aaa")
        then:
        updateEqualsTo(document, ['$set': ['id': 1, 'name': 'aaa']])

        when:
        document.clearUpdateCollector()
        document.setDataDocument(parseDataDocument(['value': 'v1', 'main': ['name': 'v1']]))
        then:
        updateEqualsTo(document, ['$set': ['dataDocument': ['value': 'v1', 'main': ['name': 'v1']]]])

        when:
        document.clearUpdateCollector()
        document.dataDocument.value = 'v2'
        document.dataDocument.main.name = 'v2'
        then:
        updateEqualsTo(document, ['$set': ['dataDocument.value': 'v2', 'dataDocument.main.name': 'v2']])

        when:
        document.dataDocument.value = 'v3'
        document.dataDocument.main.name = null
        then:
        updateEqualsTo(document, ['$set': ['dataDocument.value': 'v3'], '$unset': ['dataDocument.main.name': '']])

        when:
        document.dataDocument = null
        then:
        updateEqualsTo(document, ['$unset': ['dataDocument': '']])
    }

    def "test nested container update recoding"() {
        given:
        MainDocument document = parseMainDocument(['map': [1: ['main': ['map': [2: ['value': 'v1']]]]]])
        document.enableUpdateCollect()
        when:
        document.map.get(1).main.map.get(2).value = 'v2'
        then:
        updateEqualsTo(document, ['$set': ['map.1.main.map.2.value': 'v2']])
    }

    def "should update entire collection when modify inside collection element"() {
        given:
        MainDocument document = parseMainDocument(['list': [['value': 'v']],
                                                   'set' : [['value': 'v']],
                                                   'map2': [1: [['name': 'v']]]])
        document.enableUpdateCollect()
        when:
        document.list[0].value = 'v1'
        document.set.iterator().next().value = 'v2'
        document.map2.get(1)[0].name = 'v3'
        then:
        updateEqualsTo(document, ['$set': ['list'  : [['value': 'v1']], 'set': [['value': 'v2']],
                                           'map2.1': [['name': 'v3']]]])
    }

    def "should update by list index when use list.set"() {
        given:
        MainDocument document = parseMainDocument(['list': [[:], [:], [:]],
                                                   'map2': [1: [[:], [:], [:]]]])
        document.enableUpdateCollect()

        when:
        document.list[1] = parseDataDocument(['value': 'v1'])
        document.map2.get(1)[2] = parseMainDocument(['name': 'v1'])
        then:
        updateEqualsTo(document, ['$set': ['list.1'  : ['value': 'v1'],
                                           'map2.1.2': ['name': 'v1']]])

        when: 'subList & listIterator'
        def listIterator = document.list.subList(1, 3).listIterator()
        listIterator.next()
        listIterator.set(parseDataDocument(['value': 'v2']))
        then:
        updateEqualsTo(document, ['$set': ['list.1'  : ['value': 'v2'],
                                           'map2.1.2': ['name': 'v1']]])

        when: 'update inside element'
        document.list[1].value = 'v3'
        document.map2.get(1)[2].name = 'v3'
        then: 'should merge to total collection update'
        updateEqualsTo(document, ['$set': ['list'  : [[:], ['value': 'v3'], [:]],
                                           'map2.1': [[:], [:], ['name': 'v3']]]])
    }

    def "should list.add/list.remove project to push/[set entire list]"() {
        given:
        MainDocument document = parseMainDocument(['list': []])
        document.enableUpdateCollect()

        when:
        document.list.add(parseDataDocument(['value': 'v1']))
        then:
        updateEqualsTo(document, ['$push': ['list': ['value': 'v1']]])

        when:
        document.list.addAll([parseDataDocument(['value': 'v2']), parseDataDocument(['value': 'v3'])])
        then:
        updateEqualsTo(document, ['$push': ['list': ['$each': [['value': 'v1'], ['value': 'v2'], ['value': 'v3']]]]])

        when:
        document.clearUpdateCollector()
        document.list.remove(parseDataDocument(['value': 'v3']))
        then:
        updateEqualsTo(document, ['$set': ['list': [['value': 'v1'], ['value': 'v2']]]])
    }

    def "should set.add/set.remove project to addToSet/pull when set's element is immutable"() {
        given:
        MainDocument document = parseMainDocument(['simpleSet': []])
        document.enableUpdateCollect()

        when:
        document.simpleSet.add(1)
        then:
        updateEqualsTo(document, ['$addToSet': ['simpleSet': 1]])

        when:
        document.simpleSet.addAll([2, 3])
        then:
        updateEqualsTo(document, ['$addToSet': ['simpleSet': ['$each': [1, 2, 3]]]])

        when:
        document.clearUpdateCollector()
        document.simpleSet.remove(1)
        then:
        updateEqualsTo(document, ['$pull': ['simpleSet': 1]])

        when:
        document.simpleSet.removeAll([2, 3])
        then:
        updateEqualsTo(document, ['$pullAll': ['simpleSet': [1, 2, 3]]])
    }

    def "should set.add/set.remove project to addToSet/[set entire set] when set's element is mutable"() {
        given:
        MainDocument document = parseMainDocument(['set': []])
        document.enableUpdateCollect()

        when:
        document.set.add(parseDataDocument(['value': 'v1']))
        then:
        updateEqualsTo(document, ['$addToSet': ['set': ['value': 'v1']]])

        when:
        document.clearUpdateCollector()
        document.set.remove(parseDataDocument(['value': 'v1']))
        then:
        updateEqualsTo(document, ['$set': ['set': []]])
    }

    def "should record DtBsonValue type field update"() {
        given:
        MainDocument document = MainDocument.create()
        document.enableUpdateCollect()

        when:
        document.setDtBsonValue(new DtBsonString("aaa"))
        then:
        updateEqualsTo(document, ['$set': ['dtBsonValue': 'aaa']])

        when:
        document.setDtBsonValue(DtBsonNull.VALUE)
        then:
        updateEqualsTo(document, ['$set': ['dtBsonValue': null]])

        when:
        document.setDtBsonValue(null)
        then:
        updateEqualsTo(document, ['$unset': ['dtBsonValue': '']])

        when:
        DtBsonDocument document1 = new DtBsonDocument(['v1': new DtBsonArray([new DtBsonInt32(1), DtBsonNull.VALUE])])
        document.setDtBsonValue(document1)
        then:
        updateEqualsTo(document, ['$set': ['dtBsonValue': ['v1': [1, null]]]])

        when: 'clearUpdateCollector and modify inside DtBsonDocument'
        document.clearUpdateCollector()
        document1.getArray("v1")[0] = new DtBsonInt64(2)
        then:
        updateEqualsTo(document, ['$set': ['dtBsonValue.v1.0': 2L]])

        when:
        document.setDtBsonValue(new DtBsonArray([new DtBsonDocument("v", new DtBsonInt32(1)), DtBsonNull.VALUE]))
        then:
        updateEqualsTo(document, ['$set': ['dtBsonValue': [['v': 1], null]]])

        when: 'modify dtBsonValue field previous value'
        document1.put('v1', new DtBsonInt32(1))
        then: 'no effect'
        updateEqualsTo(document, ['$set': ['dtBsonValue': [['v': 1], null]]])
    }


}
