`IndexedPropertyDescriptor` 是 JavaBean 规范中用于描述 **“可按索引访问的数组型属性”** 的元数据类，功能与 `PropertyDescriptor` 类似，但 **额外提供带下标的读写方法**（如 `getFoo(int index)` / `setFoo(int index, Value val)`），**同时支持整体数组访问与单个元素访问**。

---

### 1  适用场景

| 场景 | 示例方法签名 | 说明 |
|---|---|---|
| **整体数组** | `String[] getNames()` / `void setNames(String[] arr)` | 一次性读/写整个数组 |
| **按索引访问** | `String getName(int i)` / `void setName(int i, String val)` | 只操作第 i 个元素 |

只要一个属性 **同时存在上述两种访问方式**，就用 `IndexedPropertyDescriptor` 来描述，否则用普通的 `PropertyDescriptor`。

---

### 2  构造器（常用）

```java
// 自动按规范查找方法
new IndexedPropertyDescriptor("name", BeanClass.class);

// 显式指定方法名
new IndexedPropertyDescriptor(
    "name", BeanClass.class,
    "getNames",      // 数组读
    "setNames",      // 数组写
    "getName",       // 索引读
    "setName"        // 索引写
);
```

---

### 3  核心 API

| 方法 | 用途 |
|---|---|
| `getIndexedReadMethod()` | 拿到 `getName(int)` |
| `getIndexedWriteMethod()` | 拿到 `setName(int, String)` |
| `getIndexedPropertyType()` | 返回数组元素类型 `String.class` |
| 继承自 `PropertyDescriptor` | 同时可使用 `getReadMethod()` / `getWriteMethod()` 访问整体数组方法 |

---

### 4  代码示例

```java
class Team {
    private String[] members = new String[3];
    public String[] getMembers() { return members; }
    public void setMembers(String[] m) { this.members = m; }
    public String getMember(int i) { return members[i]; }
    public void setMember(int i, String name) { members[i] = name; }
}

IndexedPropertyDescriptor ipd =
        new IndexedPropertyDescriptor("member", Team.class);

System.out.println(ipd.getIndexedPropertyType()); // class java.lang.String
System.out.println(ipd.getIndexedReadMethod());   // Method getMember(int)
System.out.println(ipd.getReadMethod());          // Method getMembers()
```

---

### 5  一句话速记

> **IndexedPropertyDescriptor = “数组属性说明书”：同时支持 `getXxx(int)` 单元素访问 和 `getXxx()` 整体数组访问，框架用它一键读写数组里的任意元素。**