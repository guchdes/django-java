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
package com.mountsea.django.bson;

import com.mountsea.django.bson.projection.*;
import com.mountsea.django.bson.projection.dtbson.DtBsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author guch
 * @since 3.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MainDocument extends DocumentNode {
    public static MainDocument create() {
        return create(MainDocument.class);
    }

    private Integer id;

    private String name;

    private DocumentList<DataDocument> list;

    private DocumentMap<Integer, DataDocument> map;

    private DocumentSet<DataDocument> set;

    private DocumentSet<Integer> simpleSet;

    private DocumentList<TreeDocumentMap<Integer, MainDocument>> list2;

    private DocumentMap<Integer, LinkedDocumentList<MainDocument>> map2;

    private DataDocument dataDocument;

    private DtBsonValue dtBsonValue;
}
