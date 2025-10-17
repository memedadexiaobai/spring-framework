在 JDK（`java.lang.reflect` + `java.lang.annotation`）里，**类、注解、方法、字段、构造器** 等概念都有对应的 **Class 对象** 来表示。它们之间形成了清晰的**继承树**，所有反射对象最终都实现了 `AnnotatedElement`，因此任何元素都能通过同一套 API 读取注解。

---

### 1  一张类图速览

```
java.lang.Object
   │
   ├─ java.lang.Class<T>                 类、接口、枚举、注解
   │
   ├─ java.lang.reflect.AnnotatedElement  (interface)
   │        ↑
   │        ├─ java.lang.reflect.AccessibleObject
   │        │        ↑
   │        │        ├─ java.lang.reflect.Constructor<T>
   │        │        ├─ java.lang.reflect.Method
   │        │        └─ java.lang.reflect.Field
   │        │
   │        ├─ java.lang.Package
   │        ├─ java.lang.reflect.Parameter
   │        └─ java.lang.reflect.Module   (JDK 9+)
```

---

### 2  常见“元素”与其对应 Class 一览

| 概念 | 对应 JDK Class | 典型获取方式 | 典型用途 |
|---|---|---|---|
| **类/接口/枚举/注解** | `java.lang.Class<T>` | `Foo.class`, `obj.getClass()` | 反射创建对象、读取类级注解 |
| **构造器** | `java.lang.reflect.Constructor<T>` | `clazz.getDeclaredConstructor(...)` | 实例化、读取构造器注解 |
| **方法** | `java.lang.reflect.Method` | `clazz.getDeclaredMethod(...)` | 调用、读取方法注解 |
| **字段** | `java.lang.reflect.Field` | `clazz.getDeclaredField(...)` | 读写、读取字段注解 |
| **形参** | `java.lang.reflect.Parameter` | `method.getParameters()` | 读取参数注解（如 `@PathVariable`） |
| **包** | `java.lang.Package` | `Foo.class.getPackage()` | 读取 `package-info.java` 里的包级注解 |
| **模块** | `java.lang.reflect.Module` | `clazz.getModule()` | 读取模块描述符里的注解（JDK 9+） |

---

### 3  关键集成关系

1. **所有可注解元素** → 实现 `AnnotatedElement`  
   统一 API：`isAnnotationPresent`, `getAnnotation`, `getAnnotations`, `getDeclaredAnnotations`, `getAnnotationsByType` 等。

2. **AccessibleObject** → 提供 `setAccessible(true)` 暴力访问能力  
   `Constructor`, `Method`, `Field` 均继承之。

3. **Class** 自身也实现 `AnnotatedElement`  
   因此可直接读取类级注解：`clazz.getAnnotation(Component.class)`。

---

### 4  速记口诀

> “类用 **Class**，构造用 **Constructor**，方法/字段/参数各用同名；  
> 它们都实现 **AnnotatedElement**，读注解一条线。”