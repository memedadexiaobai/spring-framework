`Introspector` 是 **JavaBean 规范的核心工具类**，位于 `java.beans` 包，  
**在运行时自动分析一个普通 Java 类的“属性、事件、方法”元数据**，  
并封装成统一的 `BeanInfo` 对象，供 **反射、GUI 设计器、框架（Spring、MyBatis、Jackson）** 使用。

---

### 1  核心能力（一句话）

> **“把任意 POJO 的字段 + getter/setter 映射成标准 JavaBean 属性描述，无需手写 XML。”**

---

### 2  典型 API

```java
// 1. 快速获取 BeanInfo（缓存全局共享）
BeanInfo info = Introspector.getBeanInfo(Foo.class);

// 2. 拿到所有属性描述符
PropertyDescriptor[] pds = info.getPropertyDescriptors();
for (PropertyDescriptor pd : pds) {
    System.out.println(pd.getName());          // 属性名
    System.out.println(pd.getReadMethod());    // getter
    System.out.println(pd.getWriteMethod());   // setter
}

// 3. 事件/方法描述
MethodDescriptor[] mds = info.getMethodDescriptors();
EventSetDescriptor[] eds = info.getEventSetDescriptors();
```

---

### 3  属性发现规则（JavaBean 规范）

| 规则 | 示例 |
|---|---|
| **读属性** | 存在 `public T getXxx()` 或 `boolean isXxx()` |
| **写属性** | 存在 `public void setXxx(T t)` |
| **属性名** | 去掉 `get/set/is` 后首字母小写 → `xxx` |
| **类型** | 由 getter 返回类型或 setter 参数类型决定 |

---

### 4  与反射的区别

| 维度 | 反射 | Introspector |
|---|---|---|
| **关注点** | 任意字段/方法 | **只认 getter/setter** |
| **规范** | 无 | 遵循 JavaBean 命名约定 |
| **缓存** | 无 | **全局缓存**（`ThreadGroupContext`） |
| **用途** | 通用 | 框架属性绑定、GUI 设计器 |

---

### 5  常见框架使用场景

- **Spring MVC** 参数绑定
- **MyBatis ResultSet → POJO**
- **Jackson JSON 属性序列化**
- **IDE 可视化设计器**（NetBeans、Eclipse WindowBuilder）

---

### 6  性能提示

- `Introspector.getBeanInfo` 结果会被 **全局缓存**，**第一次慢，后续极快**。
- 若类结构动态变化，可调用 `Introspector.flushCaches()` 或 `flushFromCaches(Class)` 清除。

---

### 7  一句话速记

> **Introspector = “JavaBean 扫描仪”：按规范把 getter/setter 翻译成属性描述，全局缓存，供框架快速读写 POJO。**