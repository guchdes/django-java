package com.mountsea.django.bson.projection.containerlisten


import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap

/**
 * @author guch
 * @since 3.0.0
 */
class ListenableMapSpecification extends Specification {

    MapListener<Integer, Integer> listener = Mock() {
    }

    static testListenableMaps = [{ l -> new ListenableMap<>(new HashMap<>(), l) },
                                 { l -> new ListenableMap<>(new LinkedHashMap<>(), l) },
                                 { l -> new ListenableMap<>(new TreeMap<>(), l) },
                                 { l -> new ListenableMap<>(new ConcurrentHashMap<>(), l) },
                                 { l -> new ListenableNavigableMap<>(new TreeMap<>(), l) }]

    def "test ListenableMap basic functions"() {
        given:
        Map<Integer, Integer> map = listenableMaps(listener)

        when:
        map.put(1, 1)
        map.put(1, 11)
        then:
        1 * listener.afterPut({ it.is(map) }, 1, 1, null)
        1 * listener.afterPut({ it.is(map) }, 1, 11, 1)
        map == [1: 11]

        when:
        map.putAll([2: 22, 1: 111])
        then:
        1 * listener.afterPut({ it.is(map) }, 2, 22, null)
        1 * listener.afterPut({ it.is(map) }, 1, 111, 11)
        map == [1: 111, 2: 22]

        when:
        map.remove(2)
        map.remove(1, 1)
        then:
        1 * listener.afterRemove({ it.is(map) }, 2, 22)
        0 * listener.afterRemove({ it.is(map) }, 1, _)
        map == [1: 111]

        when:
        map.remove(1, 111)
        then:
        1 * listener.afterRemove({ it.is(map) }, 1, 111)
        map == [:]

        when:
        map.putIfAbsent(3, 3)
        map.putIfAbsent(3, 999)
        then:
        1 * listener.afterPut({ it.is(map) }, 3, 3, null)
        0 * listener.afterPut({ it.is(map) }, 3, _, null)
        map == [3: 3]

        when:
        map.replace(4, 4)
        map.replace(3, 3, 33)
        then:
        0 * listener.afterPut({ it.is(map) }, 4, _, _)
        1 * listener.afterPut({ it.is(map) }, 3, 33, 3)
        map == [3: 33]

        when:
        map.computeIfAbsent(4, { 4 })
        map.computeIfAbsent(4, { 44 })
        then:
        1 * listener.afterPut({ it.is(map) }, 4, 4, null)
        0 * listener.afterPut({ it.is(map) }, 4, _, _)
        map == [3: 33, 4: 4]

        when:
        map.computeIfPresent(5, { k, v -> v * 2 })
        map.computeIfPresent(4, { k, v -> v * 2 })
        map.computeIfPresent(3, { k, v -> null })
        then:
        1 * listener.afterPut({ it.is(map) }, 4, 8, 4)
        0 * listener.afterPut({ it.is(map) }, 5, _, _)
        1 * listener.afterRemove({ it.is(map) }, 3, 33)
        map == [4: 8]

        when:
        map.compute(4, { k, v -> v * 2 })
        map.compute(4, { k, v -> null })
        then:
        1 * listener.afterPut({ it.is(map) }, 4, 16, 8)
        1 * listener.afterRemove({ it.is(map) }, 4, 16)
        map == [:]

        when:
        map.put(1, 1)
        map.merge(1, 2, { v1, v2 -> v1 + v2 })
        map.merge(1, 2, { v1, v2 -> null })
        then:
        1 * listener.afterPut({ it.is(map) }, 1, 3, 1)
        1 * listener.afterRemove({ it.is(map) }, 1, 3)
        map == [:]

        when:
        map.putAll([1: 1, 2: 2])
        map.replaceAll({ k, v -> v * 2 })
        then:
        1 * listener.afterPut({ it.is(map) }, 1, 2, 1)
        1 * listener.afterPut({ it.is(map) }, 2, 4, 2)
        map == [1: 2, 2: 4]

        when:
        map.clear()
        then:
        1 * listener.beforeClear({ it.is(map) })
        map == [:]

        where:
        listenableMaps << testListenableMaps
    }

    def "test ListenableMap keySet()/entrySet()/values() functions"() {
        given:
        Map<Integer, Integer> map = listenableMaps(listener)

        when:
        map.put(1, 1)
        def iterator = map.entrySet().iterator()
        def entry = iterator.next()
        entry.setValue(11)
        iterator.remove()
        then:
        1 * listener.afterPut({ it.is(map) }, 1, 11, 1)
        1 * listener.afterRemove({ it.is(map) }, 1, 11)
        map == [:]

        when:
        map.putAll([1: 1, 2: 2])
        map.keySet().remove(1)
        def keySetIterator = map.keySet().iterator()
        keySetIterator.next()
        keySetIterator.remove()
        then:
        1 * listener.afterRemove({ it.is(map) }, 1, 1)
        1 * listener.afterRemove({ it.is(map) }, 2, 2)
        map == [:]

        when:
        map.put(1, 1)
        def valuesIterator = map.values().iterator()
        valuesIterator.next()
        valuesIterator.remove()
        then:
        1 * listener.afterRemove({ it.is(map) }, 1, 1)
        map == [:]

        where:
        listenableMaps << testListenableMaps
    }

    def "test ListenableNavigableMap functions"() {
        given:
        NavigableMap<Integer, Integer> map = new ListenableNavigableMap<>(new TreeMap<>(), listener)
        map.putAll([1: 1, 2: 2, 3: 3, 4: 4, 5: 5])

        when:
        map.pollFirstEntry()
        map.pollLastEntry()
        then:
        1 * listener.afterRemove({ it.is(map) }, 1, 1)
        1 * listener.afterRemove({ it.is(map) }, 5, 5)

        when:
        def subMap = map.subMap(1, 5).subMap(2, 5)
        subMap.remove(2)
        then:
        1 * listener.afterRemove({ it.is(map) }, 2, 2)
        subMap == [3: 3, 4: 4]

        when:
        def iterator = subMap.entrySet().iterator()
        def entry = iterator.next()
        entry.setValue(33)
        iterator.remove()
        then:
        1 * listener.afterPut({ it.is(map) }, 3, 33, 3)
        1 * listener.afterRemove({ it.is(map) }, 3, 33)
        subMap == [4: 4]

        when:
        subMap.keySet().remove(4)
        then:
        1 * listener.afterRemove({ it.is(map) }, 4, 4)
        subMap == [:]

        when:
        subMap.put(4, 4)
        then:
        1 * listener.afterPut({ it.is(map) }, 4, 4, null)
        subMap == [4:4]
    }

    def "should call IncomingMapValueTransformer correctly"() {
        given:
        MapListener<Integer, Integer> listener = Mock() {
            getIncomingMapValueTransformer() >> new MapListener.IncomingMapValueTransformer<Integer, Integer>() {
                @Override
                Integer transform(Integer k, Integer v) {
                    return -v
                }
            }
        }
        Map<Integer, Integer> map = new ListenableMap<>(new HashMap<>(), listener)

        when:
        map[1] = 1
        then:
        map == [1: -1]

        when:
        map.putAll([2: 2])
        then:
        map == [1: -1, 2: -2]

        when:
        map.putIfAbsent(3, 3)
        then:
        map == [1: -1, 2: -2, 3: -3]

        when:
        map.replace(1, 11)
        then:
        map == [1: -11, 2: -2, 3: -3]

        when:
        map.replace(1, -11, 111)
        then:
        map == [1: -111, 2: -2, 3: -3]

        when:
        map.merge(2, 4, { v1, v2 -> v1 + v2 })
        then:
        map == [1: -111, 2: -2, 3: -3]

        when:
        map.clear()
        map.computeIfAbsent(1, { k -> 1 })
        then:
        map == [1: -1]

        when:
        map.computeIfPresent(1, { k, v -> 11 })
        then:
        map == [1: -11]

        when:
        map.compute(1, { k, v -> 11 })
        then:
        map == [1: -11]

        when:
        def entry = map.entrySet().iterator().next()
        entry.setValue(111)
        then:
        map == [1: -111]
    }

}
