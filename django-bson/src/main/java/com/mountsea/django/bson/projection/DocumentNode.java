/**
 * MIT License
 * <p>
 * Copyright (c) 2021 the original author or authors.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.mountsea.django.bson.projection;

import com.mountsea.django.bson.BsonUtils;
import com.mountsea.django.bson.projection.dtbson.DtBsonArray;
import com.mountsea.django.bson.projection.dtbson.DtBsonDocument;
import com.mountsea.django.bson.projection.pojo.DocumentNodeFiller;
import com.mountsea.django.bson.projection.pojo.GlobalModels;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 所有根文档类、嵌套文档类、容器节点类的父类。
 * <p>
 * DocumentNode类的属性必须同时有Field/Getter/Setter。
 * <p>
 * DocumentNode的字段类型限制为以下几种：
 * 1 不可变类型 {@link GlobalModels#isImmutableType 用此方法判断}, 包括已知的不可变类型，自定义注册的类型，
 * 以及没有setter（或者所有的setter上有 {@link BsonIgnore}注解）的类型。
 * <p>
 * 2 容器类型，包括以下几个
 * {@link DocumentList}/{@link LinkedDocumentList}/{@link CopyOnWriteDocumentList}/
 * {@link DocumentMap}/{@link TreeDocumentMap}/{@link LinkedDocumentMap}/{@link ConcurrentDocumentMap}
 * {@link DocumentSet}/{@link TreeDocumentSet}/{@link LinkedDocumentSet}/{@link ConcurrentDocumentSet}
 * <p>
 * 3 嵌套文档类，即继承 {@link DocumentNode}的自定义类。
 * <p>
 * 如果包含了不属于这3种类型的字段，在创建对象时会抛出异常 {@link DocumentClassDefinitionException}
 * <p>
 * DtBsonValue:
 * DtBsonValue是此类型系统中实现弱类型的方法，每个 {@link org.bson.BsonValue} 类型都有对应的Dt前缀类型。比如 {@link DtBsonDocument}, {@link DtBsonArray}等.
 * 在文档类中，对字段赋值时，对象类型必须和字段类型一致，而不能是字段类型的子类型。但是DtBsonValue以及其子类型的字段没有限制。
 * 要将任意结构的类存放到DtBsonValue字段时，可以先用 {@link BsonUtils#toDtBsonDocument(Object)}方法把对象转换一下，
 * 取出时也可以用{@link BsonUtils#fromBsonDocument(BsonDocument, Class)}方法进行转换。或者可以直接创建DtBsonDocument。
 * DtBsonDocument和DtBsonArray中也会自动记录对象的修改。
 * <p>
 * 使用上的限制:
 * - DocumentNode类的属性必须同时有Field/Getter/Setter。Getter/Setter应该尽量简单，不能在Getter/Setter中调用其他Setter。
 * - DocumentNode类的字段赋值只能使用字段的setter (构造方法中除外)，否则更新无法被记录。
 * - 创建DocumentNode类的对象时要通过 {@link DocumentNode#create}方法创建，否则创建对象时会报错，建议在每个DocumentNode子类上
 * 都加入静态的create方法。
 * - 一个文档类和容器类的对象，只能同时赋值到一个父节点，否则抛出异常 {@link MultiParentException}。如果需要把同一个文档对象赋值到多个
 * 父节点，可以调用 {@link #deepCloneSelf()}方法先复制一个对象。
 *
 * @author guch
 * @since 3.0.0
 */
@Slf4j
public abstract class DocumentNode {

    protected DocumentNode() {
        if (!(this instanceof net.sf.cglib.proxy.Factory ||
                this instanceof ContainerDocumentNode ||
                this instanceof NotProxy)) {
            throw new IllegalCreateDocumentException("Please create " + this.getClass() + " through DocumentNode.create method");
        }
    }

    /**
     * @return 通过无参构造方法创建代理对象
     */
    public static <T extends DocumentNode> T create(Class<T> documentClass) {
        return create(documentClass, GlobalModels.isFillNullByEmpty(documentClass));
    }

    /**
     * 通过无参构造方法创建代理对象
     *
     * @param fillNullNodeByEmpty 指定是否要填充null字段
     */
    public static <T extends DocumentNode> T create(Class<T> documentClass, boolean fillNullNodeByEmpty) {
        T document = GlobalModels.getProxiedDocumentCreator(documentClass).create();
        if (fillNullNodeByEmpty) {
            document.fillNullNodeByEmpty();
        }
        return document;
    }

    /**
     * 通过指定构造方法和参数创建代理对象
     */
    public static <T extends DocumentNode> T create(Class<T> documentClass, Class<?>[] paramTypes, Object[] args) {
        return create(documentClass, paramTypes, args, GlobalModels.isFillNullByEmpty(documentClass));
    }

    /**
     * 通过指定构造方法和参数创建代理对象
     *
     * @param fillNullNodeByEmpty 指定是否要填充null字段
     */
    public static <T extends DocumentNode> T create(Class<T> documentClass, Class<?>[] paramTypes, Object[] args,
                                                    boolean fillNullNodeByEmpty) {
        T document = GlobalModels.getProxiedDocumentCreator(documentClass).create(paramTypes, args);
        if (fillNullNodeByEmpty) {
            document.fillNullNodeByEmpty();
        }
        return document;
    }

    /**
     * 复制自身
     */
    @SuppressWarnings("unchecked")
    public <T extends DocumentNode> T deepCloneSelf() {
        if (this instanceof Collection) {
            return (T) deepCloneCollection(collectionSupplierFromClass(this.getClass()));
        }
        BsonDocument bsonDocument = BsonUtils.toBsonDocument(this);
        return (T) BsonUtils.fromBsonDocument(bsonDocument, this.getClass());
    }

    /**
     * 将此文档复制到不记录更新的外部类型
     *
     * @param targetClass 目标类型，需要继承当前类型并实现 {@link NotProxy}
     */
    public <T extends NotProxy> T deepCloneToNotProxy(Class<T> targetClass) {
        if (!NotProxy.class.isAssignableFrom(targetClass)) {
            throw new DocumentClassDefinitionException("target type is not NotProxy. " + targetClass);
        }
        if (!realClass().isAssignableFrom(targetClass)) {
            throw new DocumentClassDefinitionException(String.format("Target type is not sub type of this. target %s, this %s",
                    targetClass, this.getClass()));
        }
        if (this instanceof ContainerDocumentNode) {
            throw new DocumentClassDefinitionException("ContainerDocumentNode should not clone by this method.");
        }
        BsonDocument bsonDocument = BsonUtils.toBsonDocument(this);
        return BsonUtils.fromBsonDocument(bsonDocument, targetClass);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends DocumentNode> realClass() {
        return (Class<? extends DocumentNode>) GlobalModels.getCGLibProxyRawClass(this.getClass());
    }

    /**
     * @see DocumentNodeFiller
     */
    @SuppressWarnings("unchecked")
    public void fillNullNodeByEmpty() {
        if (this instanceof ContainerDocumentNode) {
            throw new DocumentClassDefinitionException("Cannot fill container:" + getClass());
        }
        DocumentNodeFiller<DocumentNode> filler = GlobalModels.getDocumentNodeFiller((Class<DocumentNode>) this.getClass());
        filler.accept(this);
    }

    @SuppressWarnings("unchecked")
    protected <E> Collection<E> deepCloneCollection(Supplier<Collection<?>> collectionSupplier) {
        try {
            Collection<E> to = (Collection<E>) collectionSupplier.get();
            Collection<E> from = (Collection<E>) this;
            for (E o : from) {
                if (o instanceof DocumentNode) {
                    to.add((E) ((DocumentNode) o).deepCloneSelf());
                } else {
                    to.add(o);
                }
            }
            return to;
        } catch (Exception e) {
            throw new DocumentClassDefinitionException("clone collection failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <K, E> Map<K, E> deepCloneMap(Supplier<Map<?, ?>> mapSupplier) {
        try {
            Map<K, E> to = (Map<K, E>) mapSupplier.get();
            Map<K, E> from = (Map<K, E>) this;
            for (Map.Entry<K, E> entry : from.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof DocumentNode) {
                    to.put(entry.getKey(), (E) ((DocumentNode) value).deepCloneSelf());
                } else {
                    to.put(entry.getKey(), entry.getValue());
                }
            }
            return to;
        } catch (Exception e) {
            throw new DocumentClassDefinitionException("clone collection failed", e);
        }
    }

    protected Supplier<Collection<?>> collectionSupplierFromClass(Class<?> collectionClass) {
        return () -> {
            try {
                return (Collection<?>) collectionClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new DocumentClassDefinitionException("create collection failed", e);
            }
        };
    }

    /**
     * 作为根文档记录更新的updateCollector，初始为null，调用过 {@link #enableUpdateCollect()}之后才有值
     */
    private final SyncMongoUpdateCollector __updateCollector = createUpdateCollector();

    /**
     * 每个文档只能有一个parent。
     * 使用WeakReference避免创建应用代码逻辑之外的引用关系。
     */
    private volatile ParentInfo __parent;

    /**
     * 修改字段时加的锁，主要作用是保证在并发环境下，字段的值和UpdateCollector中一致
     */
    Object getRecordLock() {
        return __updateCollector.getLock();
    }

    protected SyncMongoUpdateCollector createUpdateCollector() {
        return new SyncMongoUpdateCollector();
    }

    void enableUpdateCollect() {
        __updateCollector.enableUpdateCollect();
    }

    /**
     * 清空更新记录，并不再记录
     */
    void disableUpdateCollect() {
        __updateCollector.disableUpdateCollect();
    }

    boolean hasEnableUpdateCollect() {
        return __updateCollector.hasEnableUpdateCollect();
    }

    ParentInfo getParent() {
        return this.__parent;
    }

    void setParent(DocumentNode node, String parentPath) {
        //加锁保证一个 Document被多线程同时赋值给多个文档时，只能赋值成功一次
        synchronized (getRecordLock()) {
            ParentInfo parent = getParent();
            if (parent != null) {
                throw new MultiParentException();
            } else {
                this.__parent = new ParentInfo(node, parentPath);
            }
        }
    }

    void unsetParent(Object parent) {
        synchronized (getRecordLock()) {
            ParentInfo parent1 = getParent();
            if (parent1 == null) {
                throw new IllegalStateException("currently no parent, expect parent:" + parent);
            } else {
                DocumentNode documentNode = parent1.getNode();
                if (documentNode != null && documentNode != parent) {
                    throw new IllegalStateException("currently parent incorrect, expect parent:"
                            + parent + ", actual:" + documentNode);
                } else {
                    this.__parent = null;
                }
            }
        }
    }

    /**
     * 记录此文档整个更新
     */
    void recordSelfAssign() {
        ParentInfo parentInfo = getParent();
        if (parentInfo != null) {
            DocumentNode node = parentInfo.getNode();
            if (node != null) {
                node.recordFieldAssign(this, this, parentInfo.parentProperty);
            }
        }
    }

    /**
     * 记录字段赋值
     *
     * @param arg           新的值
     * @param previousValue 以前的值
     * @param path          路径
     */
    void recordFieldAssign(Object arg, Object previousValue, String path) {
        recordFieldAssign(arg, previousValue, path, false);
    }

    /**
     * 记录字段赋值
     *
     * @param arg            新的值
     * @param previousValue  以前的值
     * @param path           路径
     * @param setByListIndex 是否本次调用是list.set直接触发，如果是则生成根据元素下标更新的记录
     */
    void recordFieldAssign(Object arg, Object previousValue, String path, boolean setByListIndex) {
        if (__updateCollector.hasEnableUpdateCollect()) {
            if (arg == null) {
                __updateCollector.unsetField(path, previousValue);
            } else {
                __updateCollector.setField(path, arg, previousValue);
            }
        }
        ParentInfo parentInfo = getParent();
        if (parentInfo != null) {
            DocumentNode parentNode = parentInfo.getNode();
            if (parentNode != null) {
                if (!setByListIndex && this instanceof Collection) {
                    //Collection中不记录下级元素的路径，操作变成对collection的全部更新
                    parentNode.recordFieldAssign(this, previousValue, parentInfo.parentProperty);
                } else {
                    parentNode.recordFieldAssign(arg, previousValue, getConcatPath(parentInfo.parentProperty, path));
                }
            }
        }
    }

    /**
     * 记录Collection容器操作
     *
     * @param updateCollectorConsumer 第二个参数是path，path在递归调用中被拼接
     */
    void recordCollectionOp(BiConsumer<MongoUpdateCollector, String> updateCollectorConsumer) {
        recordCollectionOp0("", updateCollectorConsumer, true);
    }

    private void recordCollectionOp0(String path, BiConsumer<MongoUpdateCollector, String> updateCollectorConsumer,
                                     boolean isStart) {
        if (__updateCollector.hasEnableUpdateCollect()) {
            if (path.isEmpty()) {
                throw new IllegalStateException("collection cannot as root");
            }
            updateCollectorConsumer.accept(__updateCollector, path);
        }
        ParentInfo parentInfo = getParent();
        if (parentInfo != null) {
            DocumentNode parentNode = parentInfo.getNode();
            if (parentNode != null) {
                if (this instanceof Collection && !isStart) {
                    //Collection中不记录下级元素的路径，操作变成对collection的全部更新
                    parentNode.recordFieldAssign(this, this, parentInfo.parentProperty);
                } else {
                    parentNode.recordCollectionOp0(getConcatPath(parentInfo.parentProperty, path),
                            updateCollectorConsumer, false);
                }
            }
        }
    }

    /**
     * 获取更新记录
     *
     * @param clear 是否清空记录
     * @return
     */
    @Nullable
    @BsonIgnore
    Bson getUpdateRecord(boolean clear) {
        if (!hasEnableUpdateCollect()) {
            return null;
        }
        synchronized (getRecordLock()) {
            SyncMongoUpdateCollector updateCollector = __updateCollector;
            if (updateCollector == null) {
                return null;
            }
            Bson bson = updateCollector.getUpdate();
            if (clear) {
                updateCollector.clearUpdate();
            }
            return bson;
        }
    }

    void clearUpdateCollector() {
        MongoUpdateCollector collector = DocumentNode.this.__updateCollector;
        if (collector != null) {
            collector.clearUpdate();
        }
    }

    @Nullable
    @BsonIgnore
    public MongoUpdateCollector.MongoUpdate getMongoUpdate() { //TODO 测试
        if (!hasEnableUpdateCollect()) {
            return null;
        }
        synchronized (getRecordLock()) {
            return __updateCollector.getMongoUpdate();
        }
    }

    private static class ParentInfo {
        final WeakReference<DocumentNode> node;
        //当前文档是父文档的哪个字段
        final String parentProperty;

        ParentInfo(DocumentNode node, String parentProperty) {
            this.node = new WeakReference<>(node);
            this.parentProperty = parentProperty;
        }

        DocumentNode getNode() {
            return node.get();
        }
    }

    private static String getConcatPath(String parent, String fieldName) {
        if (isNullOrEmpty(parent) && isNullOrEmpty(fieldName)) {
            return "";
        }
        if (isNullOrEmpty(parent)) {
            return fieldName;
        }
        if (isNullOrEmpty(fieldName)) {
            return parent;
        }
        return parent + "." + fieldName;
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
