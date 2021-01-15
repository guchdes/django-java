/**
 * MIT License
 *
 * Copyright (c) 2021 the original author or authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.guchdes.django.core;

import io.github.guchdes.django.bson.EnumCodecProvider;
import io.github.guchdes.django.bson.projection.DocumentNodeHelper;
import io.github.guchdes.django.bson.projection.NotAsRecordRoot;
import io.github.guchdes.django.bson.projection.pojo.Convention;
import io.github.guchdes.django.bson.projection.pojo.Conventions;
import io.github.guchdes.django.bson.projection.pojo.DocumentPojoCodecProvider;
import io.github.guchdes.django.bson.projection.pojo.GlobalModels;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.guchdes.django.bson.CommonCodecRegistry.DEFAULT_CODEC_REGISTRY;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * 创建 {@link CollectibleDocument} 时，自动开启 update collect
 *
 * @Author guch
 * @Since 3.0.0
 */
public class AutoEnableUCCodecRegistry implements CodecRegistry {

    public static final AutoEnableUCCodecRegistry DEFAULT_INSTANCE = new AutoEnableUCCodecRegistry();

    private final CodecRegistry codecRegistry;

    private final boolean enableUpdateCollect;

    public AutoEnableUCCodecRegistry() {
        this(true, Collections.emptyList());
    }

    public AutoEnableUCCodecRegistry(List<CodecRegistry> extendCodecRegistries) {
        this(true, extendCodecRegistries);
    }

    public AutoEnableUCCodecRegistry(boolean enableUpdateCollect, List<CodecRegistry> extendCodecRegistries) {
        this.enableUpdateCollect = enableUpdateCollect;
        List<CodecRegistry> codecRegistries = new ArrayList<>();
        codecRegistries.add(fromProviders(new DjangoModelCodecProvider()));
        codecRegistries.add(DEFAULT_CODEC_REGISTRY);
        codecRegistries.addAll(extendCodecRegistries);
        codecRegistries.add(fromProviders(
                new PojoCodecProvider(),
                new EnumCodecProvider()));
        codecRegistry = fromRegistries(codecRegistries);
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz) {
        return codecRegistry.get(clazz);
    }

    private class PojoCodecProvider implements CodecProvider {

        private final DocumentPojoCodecProvider pojoCodecProvider;

        public PojoCodecProvider() {
            List<Convention> conventions = new ArrayList<>(Conventions.DEFAULT_CONVENTIONS);
            pojoCodecProvider = DocumentPojoCodecProvider.builder().automatic(true)
                    .conventions(conventions).build();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
            if (clazz.isEnum()) {
                return null;
            }
            clazz = (Class<T>) GlobalModels.getCGLibProxyRawClass(clazz);
            Codec<T> codec = pojoCodecProvider.get(clazz, registry);
            if (codec == null) {
                return null;
            }
            return new Codec<T>() {
                @Override
                public T decode(BsonReader reader, DecoderContext decoderContext) {
                    T decode = codec.decode(reader, decoderContext);
                    //现有bson框架中codec在decode时，不知道当前是否在顶层文档。EncoderContext中有一个encodingCollectibleDocument字段可以用来判断，
                    // 但DecoderContext中没有。所以要判断是否顶层文档，需要NotAsRecordRoot接口.
                    if (enableUpdateCollect && decode instanceof CollectibleDocument &&
                            !(decode instanceof NotAsRecordRoot)) {
                        DocumentNodeHelper.enableUpdateRecord(((CollectibleDocument) decode));
                    }
                    return decode;
                }

                @Override
                public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
                    codec.encode(writer, value, encoderContext);
                }

                @Override
                public Class<T> getEncoderClass() {
                    return codec.getEncoderClass();
                }
            };
        }
    }
}
