`Parameter` **不是** `Member` 的子接口！

---

### 1  位置与继承

```java
java.lang.reflect.Parameter        // 独立接口，**直接继承 Object**
```

- 实现 `AnnotatedElement`（能读注解）
- **没有**实现 `Member`
- 通过 `Method.getParameters()` 或 `Constructor.getParameters()` 拿到

---

### 2  为什么设计成这样？

| 原因 | 解释 |
|---|---|
| **语义上不属于“类的成员”** | 形参是 **方法/构造器的局部元数据**，不是类的 **字段、方法、构造器** 本身。 |
| **无 `getDeclaringClass()` 语义** | 参数本身不“属于”类，而属于 **可执行单元**（`Executable`）。 |
| **已有 `Executable` 作为父上下文** | `Parameter` 通过 `getDeclaringExecutable()` 反向关联到方法/构造器，不需要再实现 `Member`。 |

---

### 3  典型代码

```java
Method m = Foo.class.getMethod("bar", String.class);
for (Parameter p : m.getParameters()) {
    System.out.println(p.getName());           // 参数名
    System.out.println(p.getType());           // 参数类型
    System.out.println(p.getAnnotations());    // 参数注解
}
```

---

### 4  一句话总结

> **`Parameter` 是方法/构造器的“形参”元数据，只实现 `AnnotatedElement`，**不是** `Member`，因为它本质上不是“类的成员”。**