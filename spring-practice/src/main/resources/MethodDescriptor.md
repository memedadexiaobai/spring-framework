`MethodDescriptor` 也是 `java.beans` 包中的类，  
**它封装了一个 JavaBean 方法的元数据：方法名、签名、参数、返回类型、修饰符，以及方法本身（`Method` 对象）**。  
与 `PropertyDescriptor` 不同，它**不关注 getter/setter 规则**，而是 **把任意 `public` 方法当作独立服务暴露**，供 **GUI 设计器、脚本引擎、可视化工具** 调用。

---

### 1  核心构造器

```java
// 直接包装 Method 对象
MethodDescriptor desc = new MethodDescriptor(method);

// 带显式参数类型，用于重载区分
MethodDescriptor desc = new MethodDescriptor(
        beanClass, "print", new Class[]{String.class});
```

---

### 2  核心 API

| 方法 | 说明 |
|---|---|
| `getMethod()` | 拿到底层 `Method` 对象，可直接 `invoke` |
| `getName()` | 方法名 |
| `getParameterDescriptors()` | 参数描述数组 `ParameterDescriptor[]` |
| `getDisplayName()` / `getShortDescription()` | 本地化展示文本 |
| `getMethodParameterTypes()` | 返回类型数组 `Class[]` |

---

### 3  完整示例：动态调用任意方法

```java
Object bean = new Printer();
MethodDescriptor md = new MethodDescriptor(
        bean.getClass().getMethod("print", String.class));

md.getMethod().invoke(bean, "hello world");
```

---

### 4  与 `Method` 反射的区别

| 维度 | `Method` | `MethodDescriptor` |
|---|---|---|
| **来源** | 纯反射 | **JavaBean 元数据封装** |
| **额外信息** | 无 | 显示名、描述、参数描述符（支持本地化） |
| **用途** | 通用反射 | **可视化设计器、脚本、IDE 工具** |

---

### 5  常见使用场景

- **NetBeans / Eclipse WindowBuilder** 双击按钮生成事件方法
- **JavaFX Scene Builder** 为控制器暴露方法
- **BeanShell / Groovy** 脚本调用 JavaBean 任意 public 方法
- **Spring 工具类**（早期 `BeanWrapper`）也会读取 `MethodDescriptor` 做方法匹配

---

### 6  一句话速记

> **MethodDescriptor = “方法的身份证 + 本地化名片”：把任意 public 方法封装成可展示、可脚本调用的元数据，供可视化/脚本环境使用。**