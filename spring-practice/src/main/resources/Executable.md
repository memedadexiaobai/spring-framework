`Executable` 是 JDK 里 **“可执行成员”** 的公共抽象基类，**把构造器（Constructor）和方法（Method）的共性全部收拢到一起**——  
从 Java 8 开始引入，**统一处理参数、注解、泛型、修饰符、访问控制**等逻辑。

---

### 1  继承位置

```
java.lang.Object
  ↓
java.lang.reflect.AccessibleObject
  ↓
java.lang.reflect.Executable   （抽象类）
  ├─ java.lang.reflect.Method
  └─ java.lang.reflect.Constructor<T>
```

---

### 2  核心 API（Constructor 与 Method 都能用）

| 方法 | 说明 |
|------|------|
| `getParameters()` | 获取形参数组 `Parameter[]` |
| `getParameterTypes()` | 获取原始参数类型 `Class<?>[]` |
| `getGenericParameterTypes()` | 获取带泛型的参数类型 `Type[]` |
| `getParameterAnnotations()` | 二维数组，每个参数的注解 |
| `getModifiers()` | 修饰符 `public/static/...` |
| `getName()` | 方法名或 `<init>` |
| `getExceptionTypes()` | 声明抛出的异常 |
| `getAnnotatedReturnType()` | 返回类型 + 注解（Method 独有） |
| `getAnnotatedReceiverType()` | 接收者类型注解（内部类） |
| `setAccessible(boolean)` | 暴力破解 `private/protected` |
| `isVarArgs()` | 是否可变参数 |
| `getAnnotations()` / `getDeclaredAnnotations()` | 继承自 `AnnotatedElement` |

---

### 3  代码示例

```java
public class Demo {
    private Demo(String s) {}
    public void foo(@NotNull String s) {}
}

// 统一用 Executable 处理
Executable ex = Demo.class.getDeclaredConstructor(String.class);
System.out.println(ex.getName());              // <init>
System.out.println(ex.getParameterCount());    // 1
System.out.println(ex.isVarArgs());            // false
Arrays.stream(ex.getParameters())
      .forEach(p -> System.out.println(p.getName()));
```

---

### 4  一句话速记

> **Executable = “构造器 + 方法”的公共爸爸，参数、泛型、注解、修饰符一站式操作。**