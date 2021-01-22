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

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.function.Supplier;

/**
 * @author guch
 * @since 3.0.0
 */
public class LazyCodecRegistry implements CodecRegistry {
    //每次调用应该返回(功能上)相同的codecRegistry
    private final Supplier<CodecRegistry> codecRegistrySupplier;
    private volatile CodecRegistry codecRegistry;

    public LazyCodecRegistry(Supplier<CodecRegistry> codecRegistrySupplier) {
        this.codecRegistrySupplier = codecRegistrySupplier;
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz) {
        CodecRegistry codecRegistry = this.codecRegistry;
        if (codecRegistry == null) {
            this.codecRegistry = codecRegistry = codecRegistrySupplier.get();
        }
        return codecRegistry.get(clazz);
    }
}
