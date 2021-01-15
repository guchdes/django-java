
# django-java

django-java是一个Java语言的mongodb对象文档映射(ODM)框架，目标是提供更简单的数据层操作、简化文档设计，提高开发效率。适用于文档结构繁杂且更新文档需求较多的场景，比如游戏服务器或者信息管理后台等。

### 核心特性
django-java的核心特性是支持部分更新，根据对文档的修改，自动生成更新语句，实现真正意义上的对象文档映射。
在大部分场景下，你不需要手动来构造出`filter`和`update`等mongodb语句。

### Features
- 支持结合spring boot，配置简单，快速上手。

- 一个Java根文档类对应一个mongodb的collection

- 每个根文档类都有一个key，key相当于collection的唯一索引，根据key做CRUD操作简单方便，另外也支持用`Bson`来描述复杂的Filter条件。

- 支持部分更新，自动生成更新语句。文档对象上记录了对它的修改，保存文档入库时只更新修改了的部分，而不是完全更新整个文档。支持部分更新有助于使用最合理的文档结构。

- 文档类使用专用的类型系统，为文档的定义和使用加了一些限制，但同时也使文档类定义更规范。

- 支持文档缓存层，可以根据key缓存文档。

- 提供常用数据库操作的API，包括单文档CRUD， 批量CRUD、并发控制(CAS、单字段自增等)、在事务中执行一组操作等

- 可以获取mongodb原生驱动的接口`MongoCollection`和`MongoDatabase`，用来完成此框架不支持的功能。框架内的部分接口也接受的`Bson`参数，你可以用mongodb原生驱动的`Filters`写过滤条件，用`Projections`写字段选择等。

### [Wiki Home](https://github.com/guchdes/django-java/wiki)

### Download

Gradle:
```gradle
dependencies {
  implementation 'io.github.guchdes:django:3.0.0'
}
```

Maven:
```xml
<dependency>
  <groupId>io.github.guchdes</groupId>
  <artifactId>django</artifactId>
  <version>3.0.0</version>
</dependency>
```

---
**NOTE**

此版本兼容版本号`3.+`的mongodb驱动

---

### Documentation
  * [User guide](https://github.com/guchdes/django-java/wiki): This guide contains examples on how to use django-java in your code.
  * [Change log](https://github.com/guchdes/django-java/master/CHANGELOG.md): Changes in the recent versions

