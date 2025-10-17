`DigestUtils` 是 **Spring Framework 提供的“一行代码”摘要工具类**，  
**对 `java.security.MessageDigest` 做线程安全 + 空指针安全 + 编码友好封装**，  
常用于 **密码、文件、缓存 Key、请求签名的快速 MD5/SHA 计算**。

---

### 1  核心能力（都是静态方法）

| 算法 | 方法 | 输出格式 |
|---|---|---|
| **MD5** | `md5Digest(byte[])` / `md5DigestAsHex(byte[])` | `byte[]` / **小写 32 位 HEX** |
| **SHA-1** | `shaDigest(byte[])` / `shaDigestAsHex(byte[])` | 同上 |
| **SHA-256** | `sha256Digest(byte[])` / `sha256DigestAsHex(byte[])` | 同上 |
| **SHA-512** | `sha512Digest(byte[])` / `sha512DigestAsHex(byte[])` | 同上 |
| **通用** | `digest(byte[], String algorithm)` | 自定义算法 |
| **文件** | `md5DigestAsHex(InputStream)` / `sha256DigestAsHex(InputStream)` | 带缓冲，**自动关流** |

---

### 2  使用示例（一行即可）

```java
// 1. 字符串 MD5
String md5 = DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8));
// → e10adc3949ba59abbe56e057f20f883e

// 2. 文件 SHA-256
String sha256 = DigestUtils.sha256DigestAsHex(new FileInputStream("data.zip"));
// → 32 位小写 HEX

// 3. 自定义算法
byte[] hash = DigestUtils.digest(password.getBytes(), "SHA3-256");
```

---

### 3  与 Apache Commons Codec 区别

| 维度 | Spring DigestUtils | Commons Codec DigestUtils |
|---|---|---|
| **依赖** | **spring-core 已带**，零额外 jar | 需 `commons-codec` |
| **线程安全** | ✅ 无共享状态 | ✅ |
| **流处理** | ✅ 自动关流 | ✅ |
| **算法扩展** | 支持任意 `MessageDigest` 算法 | 固定 MD5/SHA 族 |
| **大小写** | **小写 HEX**（符合 URL/签名习惯） | 默认小写，可配置 |

---

### 4  性能 & 线程安全

- 底层 `MessageDigest` 实例 **每次新建**（避免多线程竞争），**无锁**；
- 对 **大文件** 使用 **8 KB 缓冲**读取，**内存占用稳定**。

---

### 5  一句话速记

> **DigestUtils = “Spring 自带的 MD5/SHA 瑞士军刀”：一行代码完成摘要，支持字符串、字节数组、文件流，线程安全，输出小写 HEX，无需额外依赖。**