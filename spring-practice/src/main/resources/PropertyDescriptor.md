`PropertyDescriptor` 是 JavaBean 规范的核心类，位于 `java.beans` 包。  
**它封装了一个属性的“元数据”：属性名、类型、读写方法（getter/setter），并提供动态读写属性值的 API。**  
框架（Spring、MyBatis、Jackson 等）都用它完成 **POJO ↔ 配置、POJO ↔ JSON、POJO ↔ 表单** 的自动绑定。

---

### 1  核心构造器

```java
// 通过属性名 + getter + setter 创建
PropertyDescriptor pd = new PropertyDescriptor("age", User.class, "getAge", "setAge");

// 最常用：只给 Class，Introspector 自动找 getter/setter
PropertyDescriptor pd = new PropertyDescriptor("age", User.class);
```

---

### 2  核心 API（一览图）

| 方法 | 说明 |
|---|---|
| `getName()` | 属性名（去掉 get/set/is 后首字母小写） |
| `getPropertyType()` | 属性类型 `Class<?>` |
| `getReadMethod()` | getter 方法对象 `Method` |
| `getWriteMethod()` | setter 方法对象 `Method` |
| `getReadMethod().invoke(bean)` | **动态读值** |
| `getWriteMethod().invoke(bean, value)` | **动态写值** |

---

### 3  完整示例：动态读写

```java
User user = new User();
PropertyDescriptor pd = new PropertyDescriptor("name", User.class);

// 写
pd.getWriteMethod().invoke(user, "Tom");

// 读
Object name = pd.getReadMethod().invoke(user);
System.out.println(name);   // Tom
```

---

### 4  与反射字段的区别

| 维度 | `Field` | `PropertyDescriptor` |
|---|---|---|
| **访问目标** | 字段本体 | **getter/setter 方法对** |
| **遵循规范** | 无 | **JavaBean 命名约定** |
| **封装程度** | 低 | 高（类型、读写方法一体） |
| **用途** | 通用反射 | **框架属性绑定** |

---

### 5  常见框架使用

- **Spring MVC** 参数绑定
- **MyBatis** `ResultSet → POJO`
- **Jackson** `JSON ↔ POJO`
- **Spring Boot ConfigurationProperties** 注入

---

### 6  一句话速记

> **PropertyDescriptor = “一个属性的身份证”：名字、类型、getter、setter 全打包，框架用它对 POJO 做自动化读写。**