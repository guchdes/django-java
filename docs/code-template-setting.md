# 代码模板设置
假设我们写了一个这样的类：
```java
class MyData extends DocumentNode {
}
```
创建`MyData`的实例时需要调用`DocumentNode.create(MyData.class)`，
在使用上不太方面，所以建议在每个DocumentNode的实现类上，加上一个静态的create方法：
```java
class MyData extends DocumentNode {
    public static MyData create() {
        return create(MyData.class);
    }
}
```
这样可以通过`MyData.create()`创建实例。通常在IDE里面可以配置代码模板来快速创建
上面的create方法。

### IntelliJ IDEA的代码模板设置
IDEA中打开 setting - editor - live templates，在other分组中添加一项，
abbreviation填写dnc，template填写
```
public static $CLASS_NAME$ create() {
    return create($CLASS_NAME$.class);
}
```
然后点击edit variable，新增一项name为CLASS_NAME，expression为className，确定并关闭edit variable窗口。
最后设置下方的applicable context，点击change，只选中java-declaration一项即可。

设置好之后，在任意一个类中按dnc，即可快速创建create()方法。