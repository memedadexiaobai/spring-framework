在 JDK 里，`java.lang.reflect.Member` 是一个 **顶级标记接口**，它 **统一表示“类的成员”**——即 **字段、方法、构造器、枚举常量** 这些“属于类的东西”。

---

### 1  定义与继承树

```java
package java.lang.reflect;

public interface Member {
    int PUBLIC = 0;
    int DECLARED = 1;

    Class<?> getDeclaringClass();   // 这个成员属于哪个类
    String   getName();             // 成员名字
    int      getModifiers();        // 修饰符（public/static/final...）
    boolean  isSynthetic();         // 是否编译器生成
}
```

实现类（都在 `java.lang.reflect` 包）：

```
Member
 ├─ Field
 ├─ Method
 ├─ Constructor<T>
 └─ java.lang.Class（特殊：代表枚举常量、记录组件）
```

---

### 2  常见用法

```java
Class<?> clazz = Foo.class;

for (Member m : clazz.getDeclaredFields())  // 字段
    System.out.println(m.getName());

for (Member m : clazz.getDeclaredMethods()) // 方法
    System.out.println(m.getName());
```

---

### 3  一句话速记

> **`Member` 就是“类成员”的公共身份证，字段、方法、构造器都实现它，统一提供“所属类、名字、修饰符”等基础信息。**