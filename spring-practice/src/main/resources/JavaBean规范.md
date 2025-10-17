JavaBean 规范（1.01，1997）非常短小，核心目标只有一个：**让普通 Java 类能被可视化设计器、脚本、框架“无感知”地创建、配置、交互**。
除前面提到的 `PropertyDescriptor` / `MethodDescriptor` / `EventSetDescriptor` / `ParameterDescriptor` 外，规范还定义了以下 **关键角色与约定**（2025 视角仍全部有效）：

---

### 1  规范四大件（必背）

| 名称 | 作用 |
|---|---|
| **JavaBean 类本身** | 必须有无参构造器；实现 `Serializable` 可选 |
| **属性（Property）** | 通过 `getXxx`/`setXxx` 或 `isXxx` 暴露，而非公有字段 |
| **事件（Event）** | 遵循 `add<Event>Listener` / `remove<Event>Listener` 签名 |
| **方法（Method）** | 任意 `public` 方法均可被设计器暴露 |

---

### 2  核心元数据接口与类（都在 `java.beans`）

| 类 / 接口 | 功能 |
|---|---|
| **`BeanInfo`** | 一个 Bean 的 **完整元数据根节点**；可显式提供属性、方法、事件描述，也可让 `Introspector` 自动推导 |
| **`Introspector`** | **扫描器**；自动解析 Bean 类，生成 `BeanInfo`（全局缓存） |
| **`PropertyDescriptor`** | 描述一个属性（已讲） |
| **`MethodDescriptor`** | 描述一个方法（已讲） |
| **`EventSetDescriptor`** | 描述一组事件（已讲） |
| **`ParameterDescriptor`** | 描述方法参数（已讲） |
| **`IndexedPropertyDescriptor`** | 描述 **索引属性**（即有 `getXxx(int index)` / `setXxx(int index, T val)` 的数组/列表属性） |
| **`BeanDescriptor`** | 描述 Bean 本身（显示名、图标、自定义izer 类名） |
| **`PropertyEditor`** 接口 | **属性编辑器**；把字符串 → 属性值（如 `#ff0000` → `Color.RED`） |
| **`PropertyEditorSupport`** | 实现 `PropertyEditor` 的便捷基类 |
| **`Customizer`** 接口 | **可视化定制面板**；双击 Bean 时弹出专用配置窗口 |
| **`VetoableChangeListener`** | **属性否决监听器**；支持 **属性校验失败时回滚**（JSR-303 前身） |
| **`PropertyChangeListener`** / **`PropertyChangeEvent`** | **属性变化通知**；数据绑定、UI 刷新基石 |

---

### 3  命名与签名硬规则（框架都认）

| 元素 | 必须签名 | 例子 |
|---|---|---|
| 无参构造 | `public X()` | 供 `Class.newInstance()`（已废弃）/ `Objenius` 实例化 |
| 读属性 | `public T getXxx()` | 布尔型可用 `public boolean isXxx()` |
| 写属性 | `public void setXxx(T t)` | 可重载，但返回类型必须是 `void` |
| 索引属性 | `public T getXxx(int i)` / `public void setXxx(int i, T t)` | 支持 `IndexedPropertyDescriptor` |
| 事件注册 | `public void add<Event>Listener(EventListener l)` | 例如 `addActionListener` |
| 事件移除 | `public void remove<Event>Listener(EventListener l)` | 与注册成对出现 |
| 属性变化 | `public void addPropertyChangeListener(PropertyChangeListener l)` | 支持绑定/回滚 |

---

### 4  可选增强特性

| 特性 | 说明 |
|---|---|
| **PropertyEditor 注册表** | `PropertyEditorManager.registerEditor(Color.class, ColorEditor.class)` |
| **Bean 自定义器 Customizer** | 实现 `Customizer` 接口，提供 Swing 面板 |
| **属性否决 Vetoable** | 抛出 `PropertyVetoException` 可阻止属性被修改 |
| **国际化支持** | `BeanInfo.getIcon(int iconKind)` / `getDisplayName()` 返回本地化资源 |

---

### 5  现代框架用法（2025 仍生效）

| 框架 | 使用场景 |
|---|---|
| **Spring MVC** | `WebDataBinder` 用 `PropertyEditor` 把表单字符串 → 日期、数字、枚举 |
| **Spring Boot ConfigurationProperties** | `Introspector` 扫描 setter 完成属性注入 |
| **MyBatis** | `ObjectFactory` 用无参构造 + setter 创建结果对象 |
| **Jackson** | 根据 getter/setter 推断 JSON 属性 |
| **JavaFX** | `PropertyChangeListener` 实现 UI 数据绑定 |

---

### 6  一句话速记（背完即可）

> **JavaBean 规范 = “无参构造 + getter/setter 属性 + add/remove 事件 + 可选序列化 + 元数据描述符 + 属性/方法/事件三描述符”；其余 PropertyEditor、Customizer、Vetoable 都是为可视化设计器准备的锦上添花。**