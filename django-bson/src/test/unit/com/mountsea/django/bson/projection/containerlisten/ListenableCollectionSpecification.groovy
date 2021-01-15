package io.github.guchdes.django.bson.projection.containerlisten


import spock.lang.Specification

import java.util.function.Predicate

/**
 * @Author guch
 * @Since 3.0.0
 */
class ListenableCollectionSpecification extends Specification {

    ListListener<Integer> listener = Mock() {
    }

    static testLists = [{ l -> new ListenableList<>(new ArrayList<>(), l) },
                        { l -> new ListenableList<>(new LinkedList<>(), l) },
                        { l -> new ListenableCopyOnWriteArrayList<>(l) },
                        { l -> new ListenableLinkedList<>(new LinkedList<>(), l) }]

    static itrTestLists = [{ l -> new ListenableList<>(new ArrayList<>(), l) },
                           { l -> new ListenableList<>(new LinkedList<>(), l) },
                           { l -> new ListenableLinkedList<>(new LinkedList<>(), l) }]

    def "test ListenableList basic functions"() {
        given:
        def list = listenableLists(listener)

        when:
        list.add(0)
        then:
        1 * listener.afterAdd({ it.is(list) }, 0, true)
        list == [0]

        when:
        list.clear()
        list.addAll(3..5)
        then:
        1 * listener.afterAddAll({ it.is(list) }, 3..5, true)
        list == 3..5

        when:
        list.addAll(0, [1, 2])
        then:
        1 * listener.afterAddAll({ it.is(list) }, [1, 2], false)
        list == 1..5

        when: 'remove by index'
        list.remove(4)
        then:
        1 * listener.afterRemove({ it.is(list) }, 5)
        list == 1..4

        when: 'remove by element'
        list.remove((Object) 4)
        then:
        1 * listener.afterRemove({ it.is(list) }, 4)
        list == 1..3

        when:
        list.removeAll([1, 4, 5])
        then:
        1 * listener.afterRemoveAll({ it.is(list) }, [1])
        list == [2, 3]

        when:
        list.retainAll([2, 4, 5])
        then:
        1 * listener.afterRemoveAll({ it.is(list) }, [3])
        list == [2]

        when:
        list.addAll([22, 222])
        list.removeIf({ it > 10 })
        then:
        1 * listener.afterRemoveAll({ it.is(list) }, [22, 222])
        list == [2]

        when:
        list.set(0, 666)
        then:
        1 * listener.afterSet({ it.is(list) }, 0, 666, 2)
        list == [666]

        when:
        list.add(999)
        list.replaceAll({ it - 1 })
        then:
        1 * listener.afterSet({ it.is(list) }, 0, 665, 666)
        1 * listener.afterSet({ it.is(list) }, 1, 998, 999)
        list == [665, 998]

        when:
        list.clear()
        then:
        1 * listener.afterClear({ it.is(list) }, [665, 998])
        list == []

        when:
        list.addAll(3..1)
        list.sort()

        then:
        1 * listener.afterAddAll(*_)
        1 * listener.afterStructChange({ it.is(list) })
        list == 1..3

        where:
        listenableLists << testLists
    }

    def "test ListenableList Iterator functions"() {
        given:
        def list = listenableLists(listener)
        list.addAll(1..5)

        when:
        def iterator = list.iterator()
        iterator.next()
        iterator.remove()
        iterator.next()
        iterator.remove()

        then:
        1 * listener.afterRemove({ it.is(list) }, 1)
        1 * listener.afterRemove({ it.is(list) }, 2)
        list == 3..5

        when:
        def listIterator = list.listIterator(1)
        listIterator.previous()
        listIterator.remove()

        then:
        1 * listener.afterRemove({ it.is(list) }, 3)
        list == [4, 5]

        when:
        listIterator.next()
        listIterator.set(444)
        listIterator.next()
        listIterator.remove()

        then:
        1 * listener.afterSet({ it.is(list) }, 0, 444, 4)
        1 * listener.afterRemove({ it.is(list) }, 5)
        list == [444]

        where:
        listenableLists << itrTestLists
    }

    def listRemove(List list, Predicate predicate) {
        def t = [*list]
        t.removeIf(predicate)
        t
    }

    def "ListenableList subList should pass raw list and raw index"() {
        given:
        def list = listenableLists(listener)
        list.addAll(0..4)

        when:
        def subSubList = list.subList(1, 5).subList(1, 4).subList(1, 3)
        subSubList.set(1, 666)
        then:
        1 * listener.afterSet({ it.is(list) }, 4, 666, 4)
        list == [0, 1, 2, 3, 666]
        subSubList == [3, 666]

        when:
        subSubList.clear()
        then:
        1 * listener.afterRemoveAll({ it.is(list) }, [3, 666])
        list == [0, 1, 2]
        subSubList == []

        where:
        listenableLists << testLists
    }

    def "ListenableList subList.listIterator should pass raw list and raw index"() {
        given:
        def list = listenableLists(listener)
        list.addAll(0..4)

        when:
        def subSubList = list.subList(1, 5).subList(1, 4).subList(1, 3)
        subSubList.set(1, 666)
        then:
        1 * listener.afterSet({ it.is(list) }, 4, 666, 4)
        list == [0, 1, 2, 3, 666]
        subSubList == [3, 666]

        when:
        def listIterator = subSubList.listIterator(1)
        listIterator.next()
        listIterator.set(777)
        then:
        1 * listener.afterSet({ it.is(list) }, 4, 777, 666)
        list == [0, 1, 2, 3, 777]
        subSubList == [3, 777]

        when:
        listIterator.add(888)
        then:
        1 * listener.afterAdd({ it.is(list) }, 888, _)
        list == [0, 1, 2, 3, 777, 888]
        subSubList == [3, 777, 888]

        when:
        listIterator.previous()
        listIterator.previous()
        listIterator.set(999)
        then:
        1 * listener.afterSet({ it.is(list) }, 4, 999, 777)
        list == [0, 1, 2, 3, 999, 888]
        subSubList == [3, 999, 888]

        where:
        listenableLists << itrTestLists
    }

    def "ListenableSet should pass successfully added element only"() {
        given:
        SetListener<Integer> listener = Mock()
        Set<Integer> set = new ListenableSet(new HashSet<>(), listener)
        set.addAll(1..3)

        when:
        set.add(3)
        then:
        0 * listener.afterAdd(*_)

        when:
        set.add(4)
        then:
        1 * listener.afterAdd({ it.is(set) }, 4, _)

        when:
        set.addAll([3, 4, 5])
        then:
        1 * listener.afterAddAll({ it.is(set) }, [5], _)

        when:
        set.addAll([3, 4, 5])
        then:
        0 * listener.afterAddAll(*_)
    }

    def "test ListenableNavigableSet functions"() {
        given:
        SetListener<Integer> listener = Mock()
        NavigableSet<Integer> set = new ListenableNavigableSet<>(new TreeSet2<>(), listener)
        set.addAll(1..5)

        when:
        set.pollFirst()
        set.pollLast()
        then:
        1 * listener.afterRemove({ it.is(set) }, 1)
        1 * listener.afterRemove({ it.is(set) }, 5)

        when:
        def subSet = set.subSet(1, 5).subSet(2, 5)
        subSet.remove(2)
        then:
        1 * listener.afterRemove({ it.is(set) }, 2)
        subSet == new HashSet([3, 4])

        when:
        def iterator = subSet.iterator()
        iterator.next()
        iterator.remove()
        then:
        1 * listener.afterRemove({ it.is(set) }, 3)
        subSet == new HashSet([4])

        when:
        subSet.remove(4)
        then:
        1 * listener.afterRemove({ it.is(set) }, 4)
        subSet == new HashSet([])

        when:
        subSet.add(3)
        then:
        1 * listener.afterAdd({ it.is(set) }, 3, _)
        subSet == new HashSet([3])
    }

    def "should not replace old object when Set.add returns false"() {
        given:
        SetListener<String> listener = Mock()
        ListenableSet<String> set = sets(listener)
        String s1 = new String("v")
        String s2 = new String("v")
        set.add(s1)

        when:
        set.add(s2)
        then:
        0 * listener.afterAdd(*_)
        set.iterator().next().is(s1)

        where:
        sets << [{ l -> new ListenableSet<>(new HashSet2<>(), l) },
                 { l -> new ListenableSet<>(new TreeSet2<>(), l) },
                 { l -> new ListenableSet<>(new LinkedHashSet2<>(), l) },
                 { l -> new ListenableSet<>(new ConcurrentHashSet<>(), l) }]
    }

    def "should Set.remove passing real removed object to listener"() {
        given:
        SetListener<String> listener = Mock()
        ListenableSet<String> set = sets(listener)
        String s1 = new String("v")
        String s2 = new String("v")
        set.add(s1)

        when:
        set.remove(s2)
        then:
        1 * listener.afterRemove(_, { it.is(s1) })

        when:
        set.clear()
        set.add(s1)
        set.removeAll([s2])
        then:
        1 * listener.afterRemoveAll(_, { it.iterator().next().is(s1) })

        where:
        sets << [{ l -> new ListenableSet<>(new HashSet2<>(), l) },
                 { l -> new ListenableSet<>(new TreeSet2<>(), l) },
                 { l -> new ListenableSet<>(new LinkedHashSet2<>(), l) },
                 { l -> new ListenableSet<>(new ConcurrentHashSet<>(), l) }]
    }

    def "should List.remove passing real removed object to listener"() {
        given:
        ListListener<String> listener = Mock()
        List<String> list = lists(listener)
        String s1 = new String("v")
        String s2 = new String("v")
        String s3 = new String("v")
        list.addAll([s1, s2])

        when:
        list.remove(s3)
        then:
        1 * listener.afterRemove(_, { it.is(s1) })

        when:
        list.removeAll([s3])
        then:
        1 * listener.afterRemoveAll(_, { it.iterator().next().is(s2) })
        list.size() == 0

        when: 'remove element from subList'
        list.addAll([s1, s2, s3])
        def subList = list.subList(1, 3)
        subList.remove(s3)
        then:
        1 * listener.afterRemove(_, { it.is(s2) })

        when:
        if (list instanceof Deque) {
            list.clear()
            list.addAll([s1, s2])
            list.removeLastOccurrence(s3)
        }
        then:
        if (list instanceof Deque) {
            1 * listener.afterRemove(_, { it.is(s2) })
        }

        when:
        if (list instanceof Deque) {
            list.clear()
            list.addAll([s1, s2])
            list.removeFirstOccurrence(s3)
        }
        then:
        if (list instanceof Deque) {
            1 * listener.afterRemove(_, { it.is(s1) })
        }

        where:
        lists << testLists
    }

    def "should call IncomingElementTransformer correctly"() {
        given:
        ListListener<Integer> listener = Mock() {
            getIncomingElementTransformer() >> new CollectionListener.IncomingElementTransformer<Integer>() {
                @Override
                Integer transform(Integer element) {
                    return -element
                }
            }
        }
        List<Integer> list = new ListenableList<>(new ArrayList<>(), listener)

        when:
        list.add(1)
        then:
        list == [-1]

        when:
        list.addAll([2, 3])
        then:
        list == [-1, -2, -3]

        when:
        list.set(0, 11)
        then:
        list == [-11, -2, -3]

        when:
        def iterator = list.listIterator()
        iterator.next()
        iterator.set(111)
        then:
        list == [-111, -2, -3]

        when:
        iterator = list.listIterator()
        iterator.next()
        iterator.add(666)
        then:
        list == [-111, -666, -2, -3]

        when:
        def subList = list.subList(0, 2)
        subList.set(0, 1111)
        then:
        list == [-1111, -666, -2, -3]
    }

}
