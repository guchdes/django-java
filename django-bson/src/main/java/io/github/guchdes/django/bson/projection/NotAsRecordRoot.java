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
package io.github.guchdes.django.bson.projection;

/**
 * {@link CollectibleDocumentNode}的实现类（假设有一个这样的类A）对应MongoCollection，不能当做其他 {@link DocumentNode}
 * 实现类的字段使用。如果有需求要重用类A的结构，又要当做其他类的字段使用，可以创建一个类继承类A，并实现 {@link NotAsRecordRoot}接口。
 * <p>
 * 从dao接口的find/get等方法查询时，传递的class不能是NotAsRecordRoot的。
 * <p>
 * 存在这样的限制和此接口的原因可见 AutoEnableUCCodecRegistry
 *
 * @author guch
 * @since 3.0.0
 */
public interface NotAsRecordRoot {
}
