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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * 对象更新监听/记录接口
 *
 * @Author guch
 * @Since 3.0.0
 * @param <A> 累积的更新记录
 */
public interface BasicUpdateCollector<A> {

    /**
     * 获取累积的更新记录
     *
     * @return
     */
    @Nullable
    A getUpdate();

    /**
     * 获取累积的更新记录
     *
     * @param excludePaths 排除的路径，以这些路径开头的记录都将被排除
     * @return
     */
    @Nullable
    A getUpdate(Set<String> excludePaths);

    /**
     * 清空当前累积更新记录
     */
    void clearUpdate();

}
