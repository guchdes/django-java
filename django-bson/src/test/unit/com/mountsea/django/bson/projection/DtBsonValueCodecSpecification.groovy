package io.github.guchdes.django.bson.projection

import io.github.guchdes.django.bson.BsonUtils
import io.github.guchdes.django.bson.CommonCodecRegistry
import io.github.guchdes.django.bson.projection.dtbson.*
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import spock.lang.Specification

class DtBsonValueCodecSpecification extends Specification {

    static codecRegistry = new CommonCodecRegistry()

    def "test primitive types"() {

        when: "write and read with codec"
        DtBsonDocumentWriter writer = new DtBsonDocumentWriter(new DtBsonDocument())
        writer.writeStartDocument()
        writer.writeName("name")
        codec.encode(writer, value, EncoderContext.builder().build())
        DtBsonDocumentReader reader = new DtBsonDocumentReader(writer.getDocument())
        reader.readStartDocument()
        reader.readName("name")
        def decode = codec.decode(reader, DecoderContext.builder().build())
        then:
        value == decode

        when: 'using in DocumentNode'
        DtTestDocument document = DtTestDocument.create()
        addPrimitiveBsonValue(document, value)
        then:
        equalsAfterConvertBsonDocument(document)
        equalsAfterConvertDtBsonDocument(document)

        where:
        value                                                                               | codec
        DtBsonNull.VALUE                                                                    | new DtBsonNullCodec()
        new DtBsonBinary("123".getBytes())                                                  | new DtBsonBinaryCodec()
        new DtBsonBoolean(true)                                                             | new DtBsonBooleanCodec()
        new DtBsonDateTime(System.currentTimeMillis())                                      | new DtBsonDateTimeCodec()
        new DtBsonDbPointer("test", new ObjectId())                                         | new DtBsonDBPointerCodec()
        new DtBsonDouble(Double.MAX_VALUE)                                                  | new DtBsonDoubleCodec()
        new DtBsonInt32(Integer.MAX_VALUE)                                                  | new DtBsonInt32Codec()
        new DtBsonInt64(Long.MIN_VALUE)                                                     | new DtBsonInt64Codec()
        new DtBsonDecimal128(new Decimal128(Long.MAX_VALUE))                                | new DtBsonDecimal128Codec()
        new DtBsonMaxKey()                                                                  | new DtBsonMaxKeyCodec()
        new DtBsonMinKey()                                                                  | new DtBsonMinKeyCodec()
        new DtBsonJavaScript("1")                                                           | new DtBsonJavaScriptCodec()
        new DtBsonJavaScriptWithScope("1", new DtBsonDocument("value", new DtBsonInt32(1))) | new DtBsonJavaScriptWithScopeCodec(new DtBsonDocumentCodec())
        new DtBsonObjectId(new ObjectId())                                                  | new DtBsonObjectIdCodec()
        new DtBsonRegularExpression("\\d+", "")                                             | new DtBsonRegularExpressionCodec()
        new DtBsonJavaScriptWithScope("1", new DtBsonDocument("value", new DtBsonInt32(1))) | new DtBsonJavaScriptWithScopeCodec(new DtBsonDocumentCodec())
        new DtBsonString("123")                                                             | new DtBsonStringCodec()
        new DtBsonSymbol("123")                                                             | new DtBsonSymbolCodec()
        new DtBsonTimestamp(System.currentTimeMillis())                                     | new DtBsonTimestampCodec()
        new DtBsonUndefined()                                                               | new DtBsonUndefinedCodec()
    }

    def "test DtBsonDocument deepCloneSelf"() {
        given:
        DtBsonDocument document = new DtBsonDocument("v1", new DtBsonArray([new DtBsonInt32(1), DtBsonNull.VALUE]))
        expect:
        document.deepCloneSelf() == document
    }

    def "test Nested DtBsonDocument/DtBsonArray codec"() {
        given:
        DtTestDocument document = DtTestDocument.create()
        document.setBsonDocument(new DtBsonDocument())
        document.setBsonArray(new DtBsonArray())

        when:
        document.bsonDocument.put("v1", new DtBsonArray([new DtBsonInt32(1), DtBsonNull.VALUE]))
        document.bsonArray.addAll([new DtBsonString("123"), new DtBsonDocument("v", new DtBsonDateTime(1L))])
        then:
        equalsAfterConvertBsonDocument(document)
        equalsAfterConvertDtBsonDocument(document)
    }

    def "test DtBsonArray deepCloneSelf"() {
        given:
        DtBsonArray array = new DtBsonArray([new DtBsonString("123"), new DtBsonDocument("v", new DtBsonDateTime(1L))])
        expect:
        array.deepCloneSelf() == array
    }

    def "test Conversion between DtBsonDocument and BsonDocument"() {
        given:
        DtBsonDocument document = new DtBsonDocument("v1", new DtBsonArray([new DtBsonInt32(1), DtBsonNull.VALUE]))
        expect:
        DtBsonDocument.fromBsonDocument(document.toBsonDocument()) == document
    }

    def "test Conversion between DtBsonArray and BsonArray"() {
        given:
        DtBsonArray array = new DtBsonArray([new DtBsonString("123"), new DtBsonDocument("v", new DtBsonDateTime(1L))])
        expect:
        DtBsonArray.fromBsonArray(array.toBsonArray()) == array
    }

    def "should not allow null values in DtBsonArray"() {
        given:
        DtBsonArray bsonArray = new DtBsonArray()

        when:
        bsonArray.add(null)
        then:
        thrown(IllegalArgumentException)

        when:
        bsonArray.addAll([new DtBsonInt32(1), null])
        then:
        thrown(IllegalArgumentException)

        when:
        bsonArray.addAll([new DtBsonInt32(1), new DtBsonInt32(2)])
        bsonArray.set(0, null)
        then:
        thrown(IllegalArgumentException)

        when:
        def iterator = bsonArray.listIterator()
        iterator.next()
        iterator.set(null)
        then:
        thrown(IllegalArgumentException)

        when:
        iterator = bsonArray.listIterator()
        iterator.next()
        iterator.add(null)
        then:
        thrown(IllegalArgumentException)

        when:
        def subList = bsonArray.subList(0, 2)
        subList.set(0, null)
        then:
        thrown(IllegalArgumentException)
    }

    def "should not allow null values in DtBsonDocument"() {
        given:
        DtBsonDocument document = new DtBsonDocument()

        when:
        document.put("v", null)
        then:
        thrown(IllegalArgumentException)

        when:
        document.putAll(['v1': null])
        then:
        thrown(IllegalArgumentException)

        when:
        document.putIfAbsent('v', null)
        then:
        thrown(IllegalArgumentException)

        when:
        document.putAll(['v1': new DtBsonInt32(1), 'v2': new DtBsonInt32(2)])
        document.replace('v1', null)
        then:
        thrown(IllegalArgumentException)

        when:
        document.replace('v1', new DtBsonInt32(1), null)
        then:
        thrown(IllegalArgumentException)

        when:
        document.merge('v1', null, { v1, v2 -> null })
        then:
        thrown(Exception)

        when:
        document.computeIfAbsent('v3', { k -> null })
        then:
        thrown(IllegalArgumentException)

        when:
        document.computeIfPresent('v1', { k, v -> null })
        then:
        thrown(IllegalArgumentException)

        when:
        document.compute('v1', { k, v -> null })
        then:
        thrown(IllegalArgumentException)

        when:
        def entry = document.entrySet().iterator().next()
        entry.setValue(null)
        then:
        thrown(IllegalArgumentException)
    }

    def encodeAndDecodeWithBsonDocument(DtTestDocument document) {
        def bsonDocument = BsonUtils.toBsonDocument(document, codecRegistry)
        return BsonUtils.fromBsonDocument(bsonDocument, DtTestDocument.class, codecRegistry)
    }

    def encodeAndDecodeWithDtBsonDocument(DtTestDocument document) {
        def bsonDocument = BsonUtils.toDtBsonDocument(document, codecRegistry)
        return BsonUtils.fromDtBsonDocument(bsonDocument, DtTestDocument.class, codecRegistry)
    }

    def addPrimitiveBsonValue(DtTestDocument document, DtBsonValue value) {
        document.setBsonValue(value)
        if (document.bsonDocument == null) {
            document.setBsonDocument(new DtBsonDocument())
        }
        if (document.bsonArray == null) {
            document.setBsonArray(new DtBsonArray())
        }
        document.getBsonDocument().put("value", value)
        document.getBsonArray().add(value)
    }

    def equalsAfterConvertBsonDocument(DtTestDocument document) {
        def copy = encodeAndDecodeWithBsonDocument(document)
        if (copy != document) {
            println "document:" + document
            println "copy:" + copy
            return false
        } else {
            return true
        }
    }

    def equalsAfterConvertDtBsonDocument(DtTestDocument document) {
        def copy = encodeAndDecodeWithDtBsonDocument(document)
        if (copy != document) {
            println "document:" + document
            println "copy:" + copy
            return false
        } else {
            return true
        }
    }


}
