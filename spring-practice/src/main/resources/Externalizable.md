`Externalizable` 是 Java 序列化机制中的 **“高级 DIY 接口”**——  
**完全由程序员自己掌控对象的序列化与反序列化过程**，  
**不使用 Java 默认的序列化算法（不会自动读写字段）。**

---

### 1  接口定义

```java
public interface Externalizable extends java.io.Serializable {
    void writeExternal(ObjectOutput out) throws IOException;
    void readExternal(ObjectInput  in)  throws IOException, ClassNotFoundException;
}
```

- 继承 `Serializable` 只是标记“可被序列化”，**实际流程由这两个方法接管**。

---

### 2  与 `Serializable` 的核心区别

| 特性 | Serializable | Externalizable |
|---|---|---|
| **默认字段序列化** | ✅ 自动 | ❌ 完全手动 |
| **是否调用构造器** | ❌ 不调用 | ✅ **先调用 public 无参构造器**，再 `readExternal` |
| **性能/大小** | 一般 | **更小、更快**（只写需要的数据） |
| **兼容性/灵活性** | 易用 | **最高**，可自定义格式、版本、加密 |

---

### 3  使用模板

```java
public class User implements Externalizable {
    private int id;
    private String name;

    public User() { } // 必须保留 public 无参构造

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeUTF(name);          // 只写需要的字段
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id   = in.readInt();
        name = in.readUTF();
    }
}
```

---

### 4  典型场景

- **高性能 RPC**（Dubbo、Hessian）
- **与外部系统二进制协议对接**（自定义格式）
- **敏感字段脱敏/加密**（在 `writeExternal` 里加密后再写）
- **向前兼容**（手动写版本号，按版本分支读取）

---

### 5  一句话速记

> **Externalizable = “序列化 DIY 模式”：字段写什么、怎么写、读什么，全由你代码说了算，性能高、灵活性最大，但要自己维护格式与版本。**