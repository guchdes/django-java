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
package io.github.guchdes.django.bson.projection.pojo;

import org.bson.BsonObjectId;
import org.bson.types.ObjectId;

/**
 * The default IdGenerators
 *
 * @see IdGenerator
 * @since 3.10
 */
public final class IdGenerators {

    /**
     * A IdGenerator for {@code ObjectId}
     */
    public static final IdGenerator<ObjectId> OBJECT_ID_GENERATOR = new IdGenerator<ObjectId>() {

        @Override
        public ObjectId generate() {
            return new ObjectId();
        }

        @Override
        public Class<ObjectId> getType() {
            return ObjectId.class;
        }
    };

    /**
     * A IdGenerator for {@code BsonObjectId}
     */
    public static final IdGenerator<BsonObjectId> BSON_OBJECT_ID_GENERATOR = new IdGenerator<BsonObjectId>() {

        @Override
        public BsonObjectId generate() {
            return new BsonObjectId();
        }

        @Override
        public Class<BsonObjectId> getType() {
            return BsonObjectId.class;
        }
    };

    private IdGenerators(){
    }
}
