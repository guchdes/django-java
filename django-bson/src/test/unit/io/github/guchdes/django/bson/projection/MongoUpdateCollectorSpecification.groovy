package io.github.guchdes.django.bson.projection

import io.github.guchdes.django.bson.BsonConvertingSpecification

/**
 * 测试MongoUpdateCollector的操作生成和各种情况下的操作合并
 *
 * @author guch
 */
class MongoUpdateCollectorSpecification extends BsonConvertingSpecification {

    static MongoUpdateCollectorImpl collector = new MongoUpdateCollectorImpl()

    def setup() {
        collector.clearUpdate()
    }

    def 'test $set'() {
        when:
        collector.setField("f1", "v1", null)
        then:
        updateEqualsTo(collector, ['$set': ['f1': 'v1']])
    }

    def 'test $unset'() {
        when:
        collector.unsetField("f2", null)
        then:
        updateEqualsTo(collector, ['$unset': ['f2': '']])
    }

    def 'test $push'() {
        when:
        def list = []
        collector.pushArrayValue("f3", list, 1)
        collector.pushArrayValueBatch("f4", list, 1..3)
        then:
        updateEqualsTo(collector, ['$push': ['f3': 1, 'f4': ['$each': 1..3]]])
    }

    def 'test $pull'() {
        when:
        def list = []
        collector.pullArrayValue("f5", list, 1)
        collector.pullArrayValueBatch("f6", list, 1..3)
        then:
        updateEqualsTo(collector, ['$pull': ['f5': 1], '$pullAll': ['f6': 1..3]])
    }

    def 'test $addToSet'() {
        when:
        def list = []
        collector.addToSetArrayValue("f7", list, 1)
        collector.addToSetArrayValueBatch("f8", list, 1..3)
        then:
        updateEqualsTo(collector, ['$addToSet': ['f7': 1, 'f8': ['$each': 1..3]]])
    }

    def "should short path op cover long path op"() {
        given:
        def list = [666]

        when:
        collector.setField("f1.f2.f3", "asd", null)
        collector.setField("f1", "zxc", null)
        collector.unsetField("f1.f2", null)
        then:
        updateEqualsTo(collector, ['$set': ['f1': 'zxc']])

        when:
        collector.clearUpdate()
        collector.pushArrayValue("f1.f2", list, "zxc");
        collector.unsetField("f1", null)
        collector.pullArrayValue("f1.f2.f3", list, "asd");
        then:
        updateEqualsTo(collector, ['$unset': ['f1': '']])

        when:
        collector.clearUpdate()
        collector.pullArrayValue("f1.f2.f3", list, "asd");
        collector.pushArrayValue("f1", list, 1)
        collector.pushArrayValue("f1.f2", list, "zxc");
        then:
        updateEqualsTo(collector, ['$set': ['f1': [666]]])
    }

    def "should multi same path array op merge to single op"() {
        given:
        List<Integer> list = []
        when:
        collector.pushArrayValue("f1", list, 1);
        collector.pushArrayValueBatch("f1", list, 2..4);
        collector.pushArrayValue("f1", list, 5);
        then:
        updateEqualsTo(collector, ['$push': ['f1': ['$each': 1..5]]])

        when:
        collector.clearUpdate()
        collector.addToSetArrayValue("f1", list, 1);
        collector.addToSetArrayValueBatch("f1", list, 2..4);
        collector.addToSetArrayValue("f1", list, 5);
        then:
        updateEqualsTo(collector, ['$addToSet': ['f1': ['$each': 1..5]]])

        when:
        collector.clearUpdate()
        collector.pullArrayValue("f1", list, 1);
        collector.pullArrayValueBatch("f1", list, 2..4);
        collector.pullArrayValue("f1", list, 5);
        then:
        updateEqualsTo(collector, ['$pullAll': ['f1': 1..5]])
    }

    def "should set & unset op cover all other op"() {
        given:
        List<Integer> list = [666]

        when:
        collector.pushArrayValue("f1.f2", list, 1)
        collector.setField("f1.f2", "asd", null)
        collector.pullArrayValue("f1.f2.f3", list, 2)
        then:
        updateEqualsTo(collector, ['$set': ["f1.f2": "asd"]])

        when: 'unset cover set'
        collector.unsetField("f1", null)
        then:
        updateEqualsTo(collector, ['$unset': ["f1": ""]])

        when: 'set cover unset'
        collector.pushArrayValue("f1", list, 1)
        then:
        updateEqualsTo(collector, ['$set': ["f1": [666]]])

        when: 'array op merge to set'
        collector.clearUpdate()
        collector.pushArrayValue("f1.f2", list, 1)
        collector.addToSetArrayValueBatch("f1.f2", list, [1])
        then:
        updateEqualsTo(collector, ['$set': ["f1.f2": [666]]])

        when: 'array op merge to set'
        collector.clearUpdate()
        collector.pushArrayValue("f1.f2", list, 1)
        collector.addToSetArrayValueBatch("f1", list, [1])
        then:
        updateEqualsTo(collector, ['$set': ["f1": [666]]])
    }

}
