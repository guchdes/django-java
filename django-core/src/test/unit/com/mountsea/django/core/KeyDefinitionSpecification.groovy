package com.mountsea.django.core

import com.mountsea.django.bson.projection.DocumentClassDefinitionException
import com.mountsea.django.bson.projection.DocumentNode
import com.mountsea.django.core.exception.IllegalKeyDjangoException

import static com.mountsea.django.core.CollectibleDocumentTestClasses.*
import static com.mountsea.django.core.KeyDefinitionTestClasses.*

/**
 * @author guch
 */
class KeyDefinitionSpecification extends DaoOperationalSpecification {

    def "should wrong key define throws Exception"() {
        when:
        CollectibleDocumentDefinitions.getDocumentDefinition(WrongFieldNameAnno.class)
        then:
        thrown(DocumentClassDefinitionException.class)

        when:
        CollectibleDocumentDefinitions.getDocumentDefinition(WrongFieldNameKeyClassAnno.class)
        then:
        thrown(DocumentClassDefinitionException.class)

        when:
        CollectibleDocumentDefinitions.getDocumentDefinition(WrongFieldTypeClassAnno.class)
        then:
        thrown(DocumentClassDefinitionException.class)
    }

    def "test key define basics"() {
        when:
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(clazz).getKeyDefinition()
        def document = parseDocument(clazz, extractKeyDocument)
        def key = definition.getKeyExtractor().extractKey(document, false)

        then:
        definition.isId() == isId
        definition.getAnnotationKeyClass() == keyClass
        definition.getPropertyMap().keySet() == new HashSet<>(keyFiledNames)
        if (keyClass == null) {
            key == extractKey
        } else {
            documentEquals(extractKey, key)
        }
        documentEquals(extractBsonKey, definition.getKeyExtractor().extractBsonKey(document, false))
        documentEquals(extractBsonKey, definition.getBsonKeyConverter().keyToBsonDocument(key, DaoOperationalSpecification.codecRegistry, false))
        documentEquals(extractBsonKey, definition.getBsonKeyConverter().keyToBsonFilter(key, DaoOperationalSpecification.codecRegistry, false))

        where:
        clazz               | isId  | keyClass   | keyFiledNames        | extractKeyDocument                                          | extractKey                                        | extractBsonKey
        FieldNameAnno       | false | null                                | ['base']             | ['_id': 1, 'base': 'b', 'base2': 'bb']                      | 'b'                                                                 | ['base': 'b']
        KeyFieldNameIdAnno  | true  | null                                | ['id']               | ['_id': 1, 'base': 'b', 'base2': 'bb']                      | 1                                                                   | ['_id': 1]
        KeyClassAnno        | false | Key        | ['base', 'f1', 'f2'] | ['_id': 1, 'base': 'b', 'base2': 'bb', 'f1': 1, 'f2': 'f2'] | ['base': 'b', 'f1': 1, 'f2': 'f2']                                  | ['base': 'b', 'f1': 1, 'f2': 'f2']
        UseSuperClassAnno   | false | null                                | ['base2']            | ['_id': 1, 'base': 'b', 'base2': 'bb']                      | 'bb'                                                                | ['base2': 'bb']
        UseDefaultId        | true  | null                                | ['id']               | ['_id': 1L]                                                 | 1L                                                                  | ['_id': 1L]
        KeyFieldAnno        | false | null                                | ['ff']               | ['_id': 1, 'ff': 2L, 'base': 'b', 'base2': 'bb']            | 2L                                                                  | ['ff': 2L]
        StructKeyFieldAnno  | false | null                                | ['point']            | ['_id': 1, 'point': ['x': 100, 'y': 200]]                   | parseDocument(Point, ['x': 100, 'y': 200]) | ['point': ['x': 100, 'y': 200]]
        ComplexKeyClassAnno | false | ComplexKey | ['k1', 'k2', 'k3']   | ['_id': 1, 'k1': ['f1': 'f1'], 'k2': ['2': [2]], 'k3': '3'] | ['k1': ['f1': 'f1'], 'k2': ['2': [2]], 'k3': '3']                   | ['k1': ['f1': 'f1'], 'k2': ['2': [2]], 'k3': '3']
    }

    def "test single field multi property key"() {
        when:
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(StructKeyFieldAnno).getKeyDefinition()
        def point = parseDocument(Point, ['x': 100, 'y': 200])
        then:
        definition.isSingleFiledAndMultiPropKey()
        definition.getSingleFieldMultiPropMap().keySet() == new HashSet<>(['x', 'y'])
        definition.getSingleFieldMultiPropMap().get('x').get(point) == 100
        definition.getSingleFieldMultiPropMap().get('y').get(point) == 200
    }

    def "should property setter and getter of key class works"() {
        when:
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(KeyClassAnno).getKeyDefinition()
        def key = parseDocument(Key, ['base': 'b', 'f1': 1, 'f2': 'f2'])
        def key2 = new Key()
        then:
        definition.getPropertyMap().keySet() == new HashSet<>(['base', 'f1', 'f2'])
        definition.getPropertyMap().get('base').get(key) == 'b'
        definition.getPropertyMap().get('f1').get(key) == 1
        definition.getPropertyMap().get('f2').get(key) == 'f2'

        when:
        definition.getPropertyMap().get('base').set(key2, 'b')
        definition.getPropertyMap().get('f1').set(key2, 1) == 1
        definition.getPropertyMap().get('f2').set(key2, 'f2')
        then:
        key == key2
    }

    def "should throws when document key fields contains null"() {
        given:
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(clazz).getKeyDefinition()
        def document = DocumentNode.create(clazz)

        when:
        definition.keyExtractor.extractKey(document, false)
        then:
        thrown(IllegalKeyDjangoException)

        when:
        definition.keyExtractor.extractBsonKey(document, false)
        then:
        thrown(IllegalKeyDjangoException)

        when:
        def key = definition.annotationKeyClass == null ? null : parseDocument(definition.annotationKeyClass, [:])
        definition.bsonKeyConverter.keyToBsonDocument(key, DaoOperationalSpecification.codecRegistry, false)
        then:
        thrown(IllegalKeyDjangoException)

        where:
        clazz << [FieldNameAnno, KeyFieldNameIdAnno, KeyClassAnno, UseSuperClassAnno, UseDefaultId,
                  StructKeyFieldAnno, ComplexKeyClassAnno]
    }

}
