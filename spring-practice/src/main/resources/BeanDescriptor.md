`BeanDescriptor` 是 JavaBean 规范里 **“类本身”** 的元数据容器，  
**只描述整个 Bean 的全局信息**：显示名、图标、自定义器类名、是否强制使用显式 `BeanInfo` 等，  
**不涉及任何字段、方法、事件**——那是 `PropertyDescriptor` / `MethodDescriptor` / `EventSetDescriptor` 的职责。

---

### 1  核心构造器

```java
// 极简：仅指定 Bean 类
BeanDescriptor bd = new BeanDescriptor(MyBean.class);

// 完整：指定类 + 自定义器类
BeanDescriptor bd = new BeanDescriptor(
        MyBean.class,
        MyBeanCustomizer.class);   // 实现 java.beans.Customizer
```

---

### 2  核心 API

| 方法 | 说明 |
|---|---|
| `getBeanClass()` | 返回 Bean 的 `Class` 对象 |
| `getCustomizerClass()` | 返回关联的 **自定义器** 类（可为 `null`） |
| `getDisplayName()` / `setDisplayName(String)` | 本地化显示名称 |
| `getShortDescription()` / `setShortDescription(String)` | 简短描述（tooltip） |
| `setValue(String key, Object value)` | 存放任意元数据（图标、分类、优先级等） |

---

### 3  自定义器（Customizer）联动

- 若 `getCustomizerClass()` 非空，可视化设计器（NetBeans、Eclipse WindowBuilder）会在 **双击 Bean 时实例化该自定义器面板**，让用户以 **图形化方式配置整个 Bean**。
- 自定义器必须实现接口 `java.beans.Customizer`（继承 `JPanel` 并支持 `PropertyChangeListener`）。

---

### 4  代码示例：提供图标与显示名

```java
public class MyBeanInfo extends SimpleBeanInfo {
    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(MyBean.class, MyBeanCustomizer.class);
        bd.setDisplayName("订单实体");
        bd.setShortDescription("封装订单头信息与明细行");
        bd.setValue("iconColor32", "com/example/icons/order32.png");
        return bd;
    }
}
```

设计器读取后会在组件面板显示 **“订单实体”** 图标与提示。

---

### 5  一句话速记

> **BeanDescriptor = “Bean 的身份证 + 名片”：只描述类本身的名字、图标、自定义器，不涉及属性/方法/事件，供可视化设计器显示与配置。**