package com.mountsea.django.core


import com.mountsea.django.bson.projection.DocumentNodeHelper

import static com.mountsea.django.core.KeyDefinitionTestClasses.*;

class CollectibleDocumentsSpecification extends DaoOperationalSpecification {

    def "should disable update record when modify key field"() {
        given:
        ComplexKeyClassAnno document = parseDocument(ComplexKeyClassAnno,
                ['_id': 1, 'k1': ['f1': 'f1', 'f2': [1: 1]], 'k2': ['2': [2, 2]], 'k3': '3', 'data': ['f2': [11: 11]]])
        DocumentNodeHelper.enableUpdateRecord(document)

        when: 'update non key field'
        document.base = 'base'
        document.data.f1 = 'f1'
        document.data.f2[11] = 22
        then:
        DocumentNodeHelper.hasEnableUpdateCollect(document)

        when:
        document.k3 = null
        then:
        !DocumentNodeHelper.hasEnableUpdateCollect(document)

        when:
        DocumentNodeHelper.enableUpdateRecord(document)
        document.k3 = 'k3'
        then:
        !DocumentNodeHelper.hasEnableUpdateCollect(document)

        when:
        DocumentNodeHelper.enableUpdateRecord(document)
        document.k1.f2[1] = 2
        then:
        !DocumentNodeHelper.hasEnableUpdateCollect(document)

        when:
        DocumentNodeHelper.enableUpdateRecord(document)
        document.k2['2'].add(2)
        then:
        !DocumentNodeHelper.hasEnableUpdateCollect(document)
    }

    def "should disable update record when modify key (is id) field"() {
        given:
        KeyFieldNameIdAnno document = parseDocument(KeyFieldNameIdAnno, ['_id': 1, 'base': 'base1'])
        DocumentNodeHelper.enableUpdateRecord(document)

        when:
        document.base = null
        then:
        DocumentNodeHelper.hasEnableUpdateCollect(document)

        when:
        document.id = 2
        then:
        !DocumentNodeHelper.hasEnableUpdateCollect(document)

        when:
        DocumentNodeHelper.enableUpdateRecord(document)
        document.id = null
        then:
        !DocumentNodeHelper.hasEnableUpdateCollect(document)
    }

    def "test @AllowNullKeyField"() {
        when:
        def definition = CollectibleDocumentDefinitions.getDocumentDefinition(ComplexKeyClassAnno)
        then:
        !definition.allowNullKeyField

        when:
        definition = CollectibleDocumentDefinitions.getDocumentDefinition(AllowNullAnno)
        then:
        definition.allowNullKeyField
    }
}
