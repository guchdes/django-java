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
package com.mountsea.django.core;

import com.mountsea.django.bson.EnhancedMapCodec;
import com.mountsea.django.core.model.MultiEquals;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.Map;

/**
 * @author guch
 * @since 3.0.0
 */
class MultiEqualsCodec implements Codec<MultiEquals> {

    private final EnhancedMapCodec mapCodec;

    public MultiEqualsCodec(CodecRegistry codecRegistry) {
        this.mapCodec = new EnhancedMapCodec(codecRegistry);
    }

    @Override
    public MultiEquals decode(BsonReader reader, DecoderContext decoderContext) {
        Map<Object, Object> map = decoderContext.decodeWithChildContext(mapCodec, reader);
        MultiEquals multiEquals = new MultiEquals();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            multiEquals.and((String) entry.getKey(), entry.getValue());
        }
        return multiEquals;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void encode(BsonWriter writer, MultiEquals value, EncoderContext encoderContext) {
        Map<?, Object> map = value.getAllFields();
        mapCodec.encode(writer, (Map<Object, Object>) map, encoderContext.getChildContext());
    }

    @Override
    public Class<MultiEquals> getEncoderClass() {
        return MultiEquals.class;
    }
}
