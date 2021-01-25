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
package com.mountsea.django.bson.projection.pojo;

import org.bson.codecs.Codec;
/**
 * A variant of {@link org.bson.codecs.configuration.CodecProvider} that generates codecs for {@link PojoCodec}.
 *
 * <p>This is a specialized codec provider that retrieves codecs which account for type parameters associated with
 * a property. In particular this should only be used to add support for custom container types like optionals.
 * It's only applicable for use by {@link PojoCodec} registered through {@link DocumentPojoCodecProvider#builder()}.
 *
 * @since 3.6
 */
public interface PropertyCodecProvider {

    /**
     * Get a {@code Codec} using the given context, which includes, most importantly, the class and bound type parameters
     * for which a {@code Codec} is required.
     *
     * @param type the class and bound type parameters for which to get a Codec
     * @param registry the registry to use for resolving dependent Codec instances
     * @param <T> the type of the class for which a Codec is required
     * @return the Codec instance, which may be null, if this source is unable to provide one for the requested Class
     */
    <T> Codec<T> get(TypeWithTypeParameters<T> type, PropertyCodecRegistry registry);
}
