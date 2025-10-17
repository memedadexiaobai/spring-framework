`WildcardType` 是 Java **泛型反射**中的一个接口，位于 `java.lang.reflect` 包中，用于表示 **通配符类型**，例如：

```java
List<? extends Number>
List<? super Integer>
List<?>
```

这些 `? extends`、`? super`、`?` 就是 **WildcardType** 的实例。

---

### ✅ 1. 定义与作用

```java
public interface WildcardType extends Type {
    Type[] getUpperBounds(); // 获取上界
    Type[] getLowerBounds(); // 获取下界
}
```

- **上界（upper bounds）**：`? extends Number` → `Number`
- **下界（lower bounds）**：`? super Integer` → `Integer`
- **无界通配符**：`?` → 上界为 `Object`，下界为空

---

### ✅ 2. 获取方式

```java
Field field = MyClass.class.getDeclaredField("list");
Type type = field.getGenericType(); // 获取泛型类型
if (type instanceof ParameterizedType) {
    Type[] actualTypes = ((ParameterizedType) type).getActualTypeArguments();
    if (actualTypes[0] instanceof WildcardType) {
        WildcardType wildcard = (WildcardType) actualTypes[0];
        System.out.println("上界: " + Arrays.toString(wildcard.getUpperBounds()));
        System.out.println("下界: " + Arrays.toString(wildcard.getLowerBounds()));
    }
}
```

---

### ✅ 3. 示例分析

| 泛型 | 上界 | 下界 |
|---|---|---|
| `List<?>` | `Object` | 无 |
| `List<? extends Number>` | `Number` | 无 |
| `List<? super Integer>` | `Object` | `Integer` |

---

### ✅ 4. 常见用途

- **泛型框架**（如 Spring、MyBatis、Jackson）解析字段/方法参数类型
- **序列化/反序列化**时判断泛型边界
- **依赖注入**时匹配泛型类型（Spring 的 `ResolvableType` 内部使用）

---

### ✅ 一句话总结

> **WildcardType 就是 Java 反射中用来表示 `? extends` / `? super` / `?` 的接口，通过它可以获取通配符的上界和下界，广泛用于泛型解析、序列化、依赖注入等框架底层逻辑。**