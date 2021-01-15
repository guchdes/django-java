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
package io.github.guchdes.django.bson;

import com.mongodb.DBObjectCodecProvider;
import com.mongodb.DBRefCodecProvider;
import com.mongodb.DocumentToDBRefTransformer;
import com.mongodb.client.gridfs.codecs.GridFSFileCodecProvider;
import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider;
import io.github.guchdes.django.bson.projection.dtbson.DtBsonValueCodecProvider;
import io.github.guchdes.django.bson.projection.pojo.Convention;
import io.github.guchdes.django.bson.projection.pojo.Conventions;
import io.github.guchdes.django.bson.projection.pojo.DocumentPojoCodecProvider;
import io.github.guchdes.django.bson.projection.pojo.GlobalModels;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.Jsr310CodecProvider;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * @Author guch
 * @Since 3.0.0
 */
public class CommonCodecRegistry implements CodecRegistry {

    // copy from MongoClientSettings.DEFAULT_CODEC_REGISTRY
    public static final CodecRegistry DEFAULT_CODEC_REGISTRY =
            fromProviders(asList(new ValueCodecProvider(),
                    new BsonValueCodecProvider(),
                    new DtBsonValueCodecProvider(),
                    new DBRefCodecProvider(),
                    new DBObjectCodecProvider(),
                    new EnhancedDocumentCodecProvider(new DocumentToDBRefTransformer()),
                    new EnhancedIterableCodecProvider(new DocumentToDBRefTransformer()),
                    new EnhancedMapCodecProvider(new DocumentToDBRefTransformer()),
                    new GeoJsonCodecProvider(),
                    new GridFSFileCodecProvider(),
                    new Jsr310CodecProvider(),
                    new BsonCodecProvider(),
                    new ObjectCodecProvider()));

    private CodecRegistry codecRegistry;

    public CommonCodecRegistry() {
        codecRegistry = fromRegistries(DEFAULT_CODEC_REGISTRY, fromProviders(
                new PojoCodecProvider(),
                new EnumCodecProvider()));
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz) {
        return codecRegistry.get(clazz);
    }

    private static class PojoCodecProvider implements CodecProvider {

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
            return pojoCodecProvider.get(clazz, registry);
        }
    }
}
