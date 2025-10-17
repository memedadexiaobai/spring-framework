`ParameterizedType` 是 Java **泛型反射**的核心接口之一，  
**表示“带泛型参数的类型”**，例如：

```java
List<String>
Map<Integer, List<Long>>
```

这些 `List<String>`、`Map<Integer, List<Long>>` 就是 `ParameterizedType` 的实例。

---

### ✅ 1. 定义与 API

```java
public interface ParameterizedType extends Type {
    Type[] getActualTypeArguments(); // 获取<>里的“实际类型参数”
    Type getRawType();               // 获取<>前面的“原始类型”
    Type getOwnerType();             // 获取内部类的外部类类型（通常 null）
}
```

---

### ✅ 2. 示例解析

```java
Field field = MyClass.class.getDeclaredField("list");
Type type = field.getGenericType();
if (type instanceof ParameterizedType) {
    ParameterizedType pt = (ParameterizedType) type;
    System.out.println("原始类型: " + pt.getRawType());                  // interface java.util.List
    System.out.println("实际参数: " + Arrays.toString(pt.getActualTypeArguments())); // [class java.lang.String]
}
```

---

### ✅ 3. 各种泛型形态一览

| 泛型 | `getRawType()` | `getActualTypeArguments()` |
|---|---|---|
| `List<String>` | `List` | `[String]` |
| `Map<Integer, String>` | `Map` | `[Integer, String]` |
| `Map<String, ? extends Number>` | `Map` | `[String, WildcardType]` |
| `MyClass<T>.Inner` | `Inner` | `[T]` + `getOwnerType() = MyClass<T>` |

---

### ✅ 4. 嵌套泛型解析（实战）

```java
Map<String, List<Long>> map;
// 获取 List<Long>
ParameterizedType listType = (ParameterizedType) pt.getActualTypeArguments()[1];
System.out.println(listType.getRawType()); // interface java.util.List
System.out.println(listType.getActualTypeArguments()[0]); // class java.lang.Long
```

---

### ✅ 5. 常见用途

- **序列化/反序列化**（Jackson、Gson、Fastjson）解析字段真实泛型类型
- **依赖注入**（Spring `ResolvableType`）匹配 `List<User>` vs `List<Admin>`
- **ORM 映射**（MyBatis）判断 `List<Entity>` 元素类型
- **泛型工厂**（对象池、缓存）动态构造 `TypeReference`

---

### ✅ 一句话总结

> **ParameterizedType 就是 Java 反射中用来表示“带尖括号 <> 的泛型类型”的接口，通过它可以拿到原始类型和实际类型参数，是框架解析泛型的必经之路。**