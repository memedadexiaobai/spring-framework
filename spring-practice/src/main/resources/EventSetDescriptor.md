`EventSetDescriptor` 是 JavaBean 规范中用于 **描述“事件源”** 的元数据类，  
**把一组事件监听接口（Listener）及其注册/注销方法（addListener/removeListener）封装成一个“事件集合”**，  
供 **可视化设计器、脚本引擎、Spring 事件机制** 等场景 **动态绑定事件监听器**。

---

### 1  核心构造器

```java
// 最常用的：直接指定监听接口
EventSetDescriptor esd = new EventSetDescriptor(
        Button.class,           // 事件源类
        "action",               // 事件名
        ActionListener.class,   // 监听接口
        "actionPerformed"       // 事件回调方法名
);
```

---

### 2  核心 API

| 方法 | 说明 |
|---|---|
| `getListenerType()` | 监听接口 `Class<?>`（如 `ActionListener.class`） |
| `getAddListenerMethod()` / `getRemoveListenerMethod()` | 注册/注销方法 `Method` |
| `getListenerMethods()` | 监听接口里所有事件方法 `Method[]` |
| `isUnicast()` | 是否只允许一个监听器（单播） |
| `setValue(String key, Object value)` | 本地化显示文本等元数据 |

---

### 3  完整示例：动态绑定事件

```java
Object source = new Button();
EventSetDescriptor esd = new EventSetDescriptor(
        source.getClass(), "action", ActionListener.class, "actionPerformed");

Method addMethod = esd.getAddListenerMethod();
addMethod.invoke(source, (ActionListener) e -> 
        System.out.println("Button clicked!"));`EventSetDescriptor` 是 JavaBean 规范中用于 **描述“事件源”** 的元数据类，  
**把一组事件监听接口（Listener）及其注册/注销方法（addListener/removeListener）封装成一个“事件集合”**，  
供 **可视化设计器、脚本引擎、Spring 事件机制** 等场景 **动态绑定事件监听器**。

---

### 1  核心构造器

```java
// 最常用的：直接指定监听接口
EventSetDescriptor esd = new EventSetDescriptor(
        Button.class,           // 事件源类
        "action",               // 事件名
        ActionListener.class,   // 监听接口
        "actionPerformed"       // 事件回调方法名
);
```

---

### 2  核心 API

| 方法 | 说明 |
|---|---|
| `getListenerType()` | 监听接口 `Class<?>`（如 `ActionListener.class`） |
| `getAddListenerMethod()` / `getRemoveListenerMethod()` | 注册/注销方法 `Method` |
| `getListenerMethods()` | 监听接口里所有事件方法 `Method[]` |
| `isUnicast()` | 是否只允许一个监听器（单播） |
| `setValue(String key, Object value)` | 本地化显示文本等元数据 |

---

### 3  完整示例：动态绑定事件

```java
Object source = new Button();
EventSetDescriptor esd = new EventSetDescriptor(
        source.getClass(), "action", ActionListener.class, "actionPerformed");

Method addMethod = esd.getAddListenerMethod();
addMethod.invoke(source, (ActionListener) e -> 
        System.out.println("Button clicked!"));
```

---

### 4  与 Spring 事件机制的关系

- Spring 早期 `BeanWrapper` 也会读取 `EventSetDescriptor`，  
  支持 **XML 配置** `<bean>` 中 `<listener>` 子标签绑定事件。
- 现代 Spring 使用 `ApplicationEventMulticaster`，但 **底层元数据扫描仍复用 JavaBean 规范**。

---

### 5  常见使用场景

| 场景 | 用途 |
|---|---|
| **NetBeans GUI Builder** | 双击按钮 → 自动生成 `actionPerformed` 方法骨架 |
| **JavaFX Scene Builder** | 为控制器暴露事件监听接口 |
| **BeanShell / Groovy 脚本** | 脚本里 `button.onAction = { ... }` 底层走 `EventSetDescriptor` |
| **自定义组件库** | 让第三方组件也能被可视化设计器识别事件 |

---

### 6  一句话速记

> **EventSetDescriptor = “事件说明书”：把“监听接口 + 注册方法 + 事件回调”打包成元数据，让可视化工具或脚本一键绑定事件监听器。**
```

---

### 4  与 Spring 事件机制的关系

- Spring 早期 `BeanWrapper` 也会读取 `EventSetDescriptor`，  
  支持 **XML 配置** `<bean>` 中 `<listener>` 子标签绑定事件。
- 现代 Spring 使用 `ApplicationEventMulticaster`，但 **底层元数据扫描仍复用 JavaBean 规范**。

---

### 5  常见使用场景

| 场景 | 用途 |
|---|---|
| **NetBeans GUI Builder** | 双击按钮 → 自动生成 `actionPerformed` 方法骨架 |
| **JavaFX Scene Builder** | 为控制器暴露事件监听接口 |
| **BeanShell / Groovy 脚本** | 脚本里 `button.onAction = { ... }` 底层走 `EventSetDescriptor` |
| **自定义组件库** | 让第三方组件也能被可视化设计器识别事件 |

---

### 6  一句话速记

> **EventSetDescriptor = “事件说明书”：把“监听接口 + 注册方法 + 事件回调”打包成元数据，让可视化工具或脚本一键绑定事件监听器。**