package io.github.guchdes.django.core

import io.github.guchdes.django.bson.projection.DocumentClassDefinitionException
import io.github.guchdes.django.bson.projection.DocumentNode
import io.github.guchdes.django.core.exception.IllegalKeyDjangoException

/**
 * @author guch
 */
class KeyDefinitionSpecification extends DaoOperationalSpecification {

    def "should wrong key define throws Exception"() {
        when:
        CollectibleDocumentDefinitions.getDocumentDefinition(KeyDefinitionTestClasses.WrongFieldNameAnno.class)
        then:
        thrown(DocumentClassDefinitionException.class)

        when:
        CollectibleDocumentDefinitions.getDocumentDefinition(KeyDefinitionTestClasses.WrongFieldNameKeyClassAnno.class)
        then:
        thrown(DocumentClassDefinitionException.class)

        when:
        CollectibleDocumentDefinitions.getDocumentDefinition(KeyDefinitionTestClasses.WrongFieldTypeClassAnno.class)
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
        KeyDefinitionTestClasses.FieldNameAnno       | false | null                                | ['base']             | ['_id': 1, 'base': 'b', 'base2': 'bb']                      | 'b'                                                                 | ['base': 'b']
        KeyDefinitionTestClasses.KeyFieldNameIdAnno  | true  | null                                | ['id']               | ['_id': 1, 'base': 'b', 'base2': 'bb']                      | 1                                                                   | ['_id': 1]
        KeyDefinitionTestClasses.KeyClassAnno        | false | KeyDefinitionTestClasses.Key        | ['base', 'f1', 'f2'] | ['_id': 1, 'base': 'b', 'base2': 'bb', 'f1': 1, 'f2': 'f2'] | ['base': 'b', 'f1': 1, 'f2': 'f2']                                  | ['base': 'b', 'f1': 1, 'f2': 'f2']
        KeyDefinitionTestClasses.UseSuperClassAnno   | false | null                                | ['base2']            | ['_id': 1, 'base': 'b', 'base2': 'bb']                      | 'bb'                                                                | ['base2': 'bb']
        KeyDefinitionTestClasses.UseDefaultId        | true  | null                                | ['id']               | ['_id': 1L]                                                 | 1L                                                                  | ['_id': 1L]
        KeyDefinitionTestClasses.KeyFieldAnno        | false | null                                | ['ff']               | ['_id': 1, 'ff': 2L, 'base': 'b', 'base2': 'bb']            | 2L                                                                  | ['ff': 2L]
        KeyDefinitionTestClasses.StructKeyFieldAnno  | false | null                                | ['point']            | ['_id': 1, 'point': ['x': 100, 'y': 200]]                   | parseDocument(KeyDefinitionTestClasses.Point, ['x': 100, 'y': 200]) | ['point': ['x': 100, 'y': 200]]
        KeyDefinitionTestClasses.ComplexKeyClassAnno | false | KeyDefinitionTestClasses.ComplexKey | ['k1', 'k2', 'k3']   | ['_id': 1, 'k1': ['f1': 'f1'], 'k2': ['2': [2]], 'k3': '3'] | ['k1': ['f1': 'f1'], 'k2': ['2': [2]], 'k3': '3']                   | ['k1': ['f1': 'f1'], 'k2': ['2': [2]], 'k3': '3']
    }

    def "test single field multi property key"() {
        when:
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(KeyDefinitionTestClasses.StructKeyFieldAnno).getKeyDefinition()
        def point = parseDocument(KeyDefinitionTestClasses.Point, ['x': 100, 'y': 200])
        then:
        definition.isSingleFiledAndMultiPropKey()
        definition.getSingleFieldMultiPropMap().keySet() == new HashSet<>(['x', 'y'])
        definition.getSingleFieldMultiPropMap().get('x').get(point) == 100
        definition.getSingleFieldMultiPropMap().get('y').get(point) == 200
    }

    def "should property setter and getter of key class works"() {
        when:
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(KeyDefinitionTestClasses.KeyClassAnno).getKeyDefinition()
        def key = parseDocument(KeyDefinitionTestClasses.Key, ['base': 'b', 'f1': 1, 'f2': 'f2'])
        def key2 = new KeyDefinitionTestClasses.Key()
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
        clazz << [KeyDefinitionTestClasses.FieldNameAnno, KeyDefinitionTestClasses.KeyFieldNameIdAnno, KeyClassAnno, UseSuperClassAnno, UseDefaultId,
                  StructKeyFieldAnno, ComplexKeyClassAnno]
    }

}
