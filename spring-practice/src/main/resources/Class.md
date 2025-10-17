`Class` 是 Java **反射机制的入口对象**，代表 **一个类型（类、接口、数组、基本类型、void、枚举、记录、注解）** 的运行时元数据。  
**所有反射操作（创建对象、访问字段、调用方法、读取注解、判断类型关系）都必须先拿到 `Class` 对象**。

---

### 1  获取方式（4 种）

| 写法 | 场景 |
|---|---|
| `Foo.class` | 编译期已知 |
| `obj.getClass()` | 已有实例 |
| `Class.forName("com.example.Foo")` | 运行时动态加载 |
| `classLoader.loadClass("com.example.Foo")` | 自定义加载策略 |

---

### 2  核心 API 一张图（够用版）

| 类别 | 常用方法 | 说明 |
|---|---|---|
| **名称信息** | `getName()` / `getSimpleName()` / `getTypeName()` | 全限定名、简称、可读名 |
| **类型判断** | `isInterface()` / `isArray()` / `isEnum()` / `isPrimitive()` / `isRecord()` / `isAnnotation()` | 快速分型 |
| **继承关系** | `getSuperclass()` / `getInterfaces()` / `getGenericInterfaces()` | 父类、接口（含泛型） |
| **加载器** | `getClassLoader()` | 拿到所属 `ClassLoader` |
| **反射创建** | `newInstance()`（已弃用）→ `getDeclaredConstructor().newInstance()` | 无参构造快速创建 |
| **成员访问** | `getDeclaredFields()` / `getDeclaredMethods()` / `getDeclaredConstructors()` | **本类声明**（含 private） |
| **公开成员** | `getFields()` / `getMethods()` / `getConstructors()` | **public + 继承链** |
| **注解** | `getAnnotations()` / `getDeclaredAnnotations()` / `getAnnotation(Class<A>)` | 读取注解 |
| **数组** | `getComponentType()` / `isArray()` | 数组元素类型 |
| **类型比较** | `isAssignableFrom(Class<?> cls)` / `isInstance(Object obj)` | 父 ← 子 判断 |

---

### 3  代码速敲

```java
Class<?> clz = User.class;
System.out.println(clz.getSimpleName());                 // User
System.out.println(clz.isInterface());                   // false
System.out.println(clz.getSuperclass());                 // class java.lang.Object
System.out.println(Arrays.toString(clz.getInterfaces()));// [interface java.io.Serializable]
System.out.println(clz.getDeclaredConstructor().newInstance()); // User 实例
```

---

### 4  一句话速记

> **Class = “类型身份证”：拿到它，就能反射创建对象、访问成员、读注解、判类型，是 Java 元编程的总入口。**
> 
> 
> 
> `isAssignableFrom` 是 `Class` 类的一个 **反射工具方法**，用来 **判断当前类型是否能接收另一个类型的值**（即 **“父子/兼容”关系**）。  
它是 **编译期 `instanceof` 的动态版**，但 **方向相反**！

---

### 1  方法签名

```java
public boolean isAssignableFrom(Class<?> cls)
```

- **调用者**：父类型（或接口、数组、基本类型）
- **参数**：待检查的子类型
- **返回**：`true` 表示 **“参数 cls 可以赋值给当前类型”**

---

### 2  方向口诀（一遍记住）

> **“左边是爹，右边是娃；爹.isAssignableFrom(娃) → true”**

```java
Number.class.isAssignableFrom(Integer.class)  // true
Integer.class.isAssignableFrom(Number.class)  // false
```

---

### 3  对比 `instanceof`

| 表达式 | 含义 | 方向 |
|---|---|---|
| `obj instanceof Number` | **对象**是否是 Number 或其子类 | **对象 → 类型** |
| `Number.class.isAssignableFrom(obj.getClass())` | **obj 的类型**能否赋值给 Number | **类型 → 类型** |

---

### 4  覆盖场景速查

| 场景 | 示例 | 结果 |
|---|---|---|
| 父子类 | `List.class.isAssignableFrom(ArrayList.class)` | `true` |
| 接口与实现 | `Serializable.class.isAssignableFrom(String.class)` | `true` |
| 数组 | `Object.class.isAssignableFrom(int[].class)` | `true` |
| 基本类型 | `int.class.isAssignableFrom(int.class)` | `true`（同类型） |
| 包装 vs 基本 | `Integer.class.isAssignableFrom(int.class)` | `false`（自动拆装箱不参与） |
| 自身 | `String.class.isAssignableFrom(String.class)` | `true` |

---

### 5  实战用法（通用模板）

```java
public static boolean isStringFamily(Class<?> type) {
    return CharSequence.class.isAssignableFrom(type);
}
// 调用
System.out.println(isStringFamily(StringBuilder.class)); // true
```

---

### 6  一句话速记

> **“isAssignableFrom = 动态版 instanceof，方向反一下：左边是父，右边是子，true 表示子能赋给父。”**