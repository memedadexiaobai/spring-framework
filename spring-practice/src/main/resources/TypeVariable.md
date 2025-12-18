`TypeVariable` 是 Java 反射里“泛型变量”的代言人，专门代表那些出现在类、方法、接口声明里的“字母”——比如 `T`、`E`、`K`、`V` 等。  
把它搞懂，就能在运行时拆任何泛型签名，写真正的“泛型通杀”工具。

---

### 1. 身世与位置
```java
java.lang.reflect.TypeVariable<D extends GenericDeclaration>
```

- 顶级接口：`Type`（和 `Class`、`ParameterizedType`、`GenericArrayType`、`WildcardType` 并列）
- 泛型参数 `D`：这个变量是在哪个“泛型声明”里诞生的——只能是 `Class`、`Method`、`Constructor` 三者之一。

---

### 2. 三个核心 API
| 方法 | 含义 |
|------|------|
| `String getName()` | 拿到字母本身，如 `"T"`、`"E"` |
| `Type[] getBounds()` | 拿到上界数组；无显式 extends 时默认是 `Object` |
| `D getGenericDeclaration()` | 返回“出生地”——哪个类/方法/构造器声明了这个变量 |

---

### 3. 一段代码看懂它
```java
class Foo<T extends Number & Comparable<T>> {
    // 声明了一个 TypeVariable，名字=T，上界=[Number, Comparable<T>]
}

// 把变量抠出来
TypeVariable<?> tv = (TypeVariable<?>)
        Foo.class.getTypeParameters()[0];

System.out.println(tv.getName());              // T
System.out.println(Arrays.toString(tv.getBounds()));
// [class java.lang.Number, java.lang.Comparable<T>]
System.out.println(tv.getGenericDeclaration()); // class Foo
```

---

### 4. 常见误区
- `TypeVariable` ≠ `Class`  
  它只代表“字母”，运行时再被具体类型替换；没有 `.class` 文件，也没有 `Class` 对象。

- 擦除后拿不到？  
  反射里可以拿到，因为 `Signature` 属性会保留完整泛型签名；只是 JVM 字节码里字段/参数已擦除。

---

### 5. 实战场景
1. 写反序列化框架（FastJSON、Gson）  
   把 `List<T>` 里的 `T` 抠出来，再递归解析 JSON。
2. 实现泛型 DAO  
   `BaseDao<T>` 子类只写一次，运行时把 `T` 解析成真实实体类。
3. 框架级依赖注入  
   按泛型类型精确匹配 `Bean<ConcreteType>`。

---

### 6. 一张图总结
```
Type
 └── TypeVariable<D>      // 泛型变量“字母”
        - getName()       // 字母名
        - getBounds()     // 上界
        - getGenericDeclaration() // 出生地
```

记住：  
> `TypeVariable` 就是“泛型签名里的字母”，用它可以**在运行时把被擦除的字母重新找回来**。