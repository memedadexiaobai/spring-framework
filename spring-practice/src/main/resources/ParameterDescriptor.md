`ParameterDescriptor` 是 JavaBean 规范里 **“方法参数”** 的元数据封装类，  
**把方法形参的名字、类型、注解、描述信息打包**，  
供 **可视化设计器、脚本引擎、IDE 工具** 在 **动态调用方法** 时 **提示用户填写参数**。

---

### 1  获取方式

```java
MethodDescriptor md = new MethodDescriptor(fooMethod);
ParameterDescriptor[] pds = md.getParameterDescriptors(); // 一个元素对应一个形参
```

---

### 2  核心 API

| 方法 | 说明 |
|---|---|
| `getName()` | 参数名（需要编译时保留 `-parameters`，否则返回 `arg0`） |
| `getType()` | 参数类型 `Class<?>` |
| `getAnnotations()` | 参数上的注解数组 |
| `getShortDescription()` / `getDisplayName()` | 本地化描述，用于弹窗提示 |

---

### 3  完整示例：动态方法调用前提示参数

```java
Method method = Calculator.class.getMethod("add", int.class, int.class);
ParameterDescriptor[] pds = new MethodDescriptor(method).getParameterDescriptors();

for (int i = 0; i < pds.length; i++) {
    System.out.printf("第%d个参数：%s %s%n",
            i + 1,
            pds[i].getType().getSimpleName(),
            pds[i].getName());
}
```

输出（需 `-parameters`）：
```
第1个参数：int a
第2个参数：int b
```

---

### 4  与 `Parameter` 反射的区别

| 维度 | `Parameter` | `ParameterDescriptor` |
|---|---|---|
| **来源** | 纯反射 | JavaBean 元数据封装 |
| **额外信息** | 无 | 可携带 **显示名、描述、本地化文本** |
| **用途** | 通用反射 | **可视化/脚本环境** 参数提示 |

---

### 5  常见使用场景

- **NetBeans / Eclipse WindowBuilder**  
  双击按钮 → 为 `actionPerformed(ActionEvent e)` 弹窗提示 `e` 参数描述。
- **BeanShell / Groovy 控制台**  
  脚本调用方法前，列出参数名与类型。
- **Spring Tool Suite**  
  XML 配置 `<constructor-arg>` 时提示参数名。

---

### 6  一句话速记

> **ParameterDescriptor = “方法参数的身份证 + 名片”：把形参名字、类型、注解、描述打包，供可视化工具或脚本在调用前提示用户。**