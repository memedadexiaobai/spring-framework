`FastByteArrayOutputStream` 是 **Spring 提供的高性能内存字节输出流**，  
**摒弃 JDK `ByteArrayOutputStream` 的“锁 + 数组拷贝”策略**，  
**使用 **“分段链表（chunks）”** 存储数据，写操作 **无锁、无拷贝、可并行**，**  
**直到真正需要 `byte[]` 时才一次性合并**，  
**非常适合 **高并发、大缓冲、临时序列化** 场景（如 Spring MVC 消息转换、Tomcat Gzip、FST/Kryo 序列化）。**

---

### 1  痛点对比（2024-2025 实测）

| 维度 | JDK `ByteArrayOutputStream` | Spring `FastByteArrayOutputStream` |
|---|---|---|
| **扩容策略** | `Arrays.copyOf` 每次 **全量拷贝** | **链表追加新 chunk**，**零拷贝** |
| **锁** | `synchronized` 写锁 | **无锁**，分段追加 |
| **并发写入** | **性能陡降** | **可并行写不同实例** |
| **大缓冲（>8 MB）** | 频繁 **OOM + GC 停顿** | **链表平滑增长** |
| **toByteArray()** | **额外一次拷贝** | **仅最终合并**（可复用） |

---

### 2  核心结构（源码速览）

```java
// 默认 chunk 大小 4 KB
private static final int DEFAULT_BLOCK_SIZE = 4096;
// 链表头
private final LinkedList<byte[]> buffers = new LinkedList<>();
// 当前写指针
private byte[] currentBuffer;
private int currentBufferPos;
```

- **写满一个 chunk → 新建 chunk 追加到链表** → **无数组拷贝**。
- **toByteArray()** 只在 **真正需要** 时 **顺序拷贝一次**（可复用内部数组，避免二次分配）。

---

### 3  关键 API（够用即战）

| 方法 | 说明 |
|---|---|
| `write(int b)` / `write(byte[] b)` | 无锁追加，自动扩容 chunk |
| `writeTo(OutputStream out)` | **零额外内存**，按 chunk 顺序写出（适合直接写网络/文件） |
| `toByteArray()` | 最终合并成 `byte[]`（可复用） |
| `toInputStream()` | 返回 **FastByteArrayInputStream**，**共享底层 chunk**，**零拷贝**读取 |
| `size()` | 已写入字节数 |
| `reset()` | 清空链表，**复用 chunk 对象**，减少 GC |

---

### 4  代码示例（Spring MVC 消息转换器内部）

```java
FastByteArrayOutputStream out = new FastByteArrayOutputStream();
objectMapper.writeValue(out, user);          // JSON 序列化无锁写
// ① 直接写给网络，零额外内存
out.writeTo(response.getOutputStream());
// ② 或一次性拿字节
byte[] bytes = out.toByteArray();
```

---

### 5  适用场景（官方注释 + 社区实践）

| 场景 | 理由 |
|---|---|
| **Spring HTTP 消息转换** | `MappingJackson2HttpMessageConverter` 默认用它缓冲 JSON |
| **Tomcat/Gzip 输出** | 分段压缩后 **无锁收集** |
| **Kryo/FST 对象序列化** | 大对象图 **快速缓冲** |
| **多线程导出/报表** | 每个线程独享实例，**并行写无竞争** |
| **大文件 MD5/SHA 计算** | **分段收集**后一次性喂给 `MessageDigest` |

---

### 6  一句话速记

> **FastByteArrayOutputStream = “链表版 ByteArrayOutputStream”：写操作无锁、无拷贝、分段增长，只在最后需要时合并一次，是高并发、大缓冲、序列化场景下的零拷贝利器。**