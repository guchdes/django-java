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

import org.bson.conversions.Bson;

/**
 * {@link DocumentNode} 的有些方法需要暴露给特定的类，但是又不适合用protect或public，
 * 所以通过此类暴露 {@link DocumentNode} 的包私有方法。
 *
 * @author guch
 * @since 3.0.0
 */
public class DocumentNodeHelper {

    public static Object getRecordLock(DocumentNode documentNode) {
        return documentNode.getRecordLock();
    }

    public static void enableUpdateRecord(DocumentNode documentNode) {
        documentNode.enableUpdateCollect();
    }

    public static void disableUpdateRecord(DocumentNode documentNode) {
        documentNode.disableUpdateCollect();
    }

    public static boolean hasEnableUpdateCollect(DocumentNode documentNode) {
        return documentNode.hasEnableUpdateCollect();
    }

    public static Bson getUpdateRecord(DocumentNode documentNode, boolean clear) {
        return documentNode.getUpdateRecord(clear);
    }

    public static void clearUpdateCollector(DocumentNode documentNode) {
        documentNode.clearUpdateCollector();
    }


}
