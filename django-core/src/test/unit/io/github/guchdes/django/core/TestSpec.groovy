package io.github.guchdes.django.core

import io.github.guchdes.django.bson.BsonUtils
import org.bson.BsonDocument
import spock.lang.Specification

/**
 * @author guch
 * @since 3.0.0
 */
class TestSpec extends Specification {

    def "sd"() {
        when:
        def a = [[[1]]].flatten()
        println a
        then:
        1==1
//
//        where :
//        b | c
//        'b' | 'c'
//        'bb' | 'cc'
//        [q,w] << [ [1, 2], [b, c]].combinations()


    }

}
