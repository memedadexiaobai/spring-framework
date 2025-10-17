`LinkedCaseInsensitiveMap` 是 **Spring Framework 提供的一个“顺序 + 忽略大小写”专用 Map**，  
**在 LinkedHashMap 基础上再包一层“小写→原 key”映射表**，  
**既保留插入顺序，又允许用任意大小写 get/remove，常用于 HTTP Header、MyBatis 结果集、配置属性等大小写不敏感场景。**

---

### 1  核心结构（双 Map 方案）

| 字段 | 类型 | 作用 |
|---|---|---|
| `targetMap` | `LinkedHashMap<String,V>` | **真实数据**，key 保持用户原始大小写，决定遍历顺序 |
| `caseInsensitiveKeys` | `HashMap<String,String>` | **索引表**，key = **小写**版本，value = 原始 key |
| `locale` | `Locale` | 转小写时使用的区域（默认可配置） |

---

### 2  读写流程（以 `get("UsErId")` 为例）

1. 把传入 key → 小写 `"userid"`
2. 用 `caseInsensitiveKeys` 找到原 key → `"userId"`
3. 用原 key 去 `targetMap` 拿值
4. **对外表现**：**大小写无感**，但 **遍历/entrySet** 仍返回 **原始大小写** 和 **插入顺序**

---

### 3  与常见 Map 对比

| 特性 | LinkedHashMap | LinkedCaseInsensitiveMap | CaseInsensitiveMap (Apache) |
|---|---|---|---|
| 顺序 | ✅ 插入顺序 | ✅ 插入顺序 | ❌ 无序 |
| 大小写 | 敏感 | **忽略** | 忽略 |
| null key | ✅ | **❌** 抛异常 | ✅ |
| 遍历 key | 原样 | **原大小写** | 全小写 |
| 并发 | 非线程安全 | 非线程安全 | 非线程安全 |

---

### 4  典型使用场景

| 场景 | 用途 |
|---|---|
| **Spring HttpHeaders** | 存储 HTTP 头，**get("content-type") / get("Content-Type")** 都能命中 |
| **MyBatis 结果集** | 数据库列名大小写不固定，**自动映射到 Java 属性** |
| **配置属性绑定** | `application.yml` 里 **server-port / SERVER_PORT** 均可识别 |
| **拦截器/过滤器** | 统一处理 **Authorization / authorization** 头 |

---

### 5  代码示例

```java
Map<String, String> map = new LinkedCaseInsensitiveMap<>();
map.put("Content-Type", "json");

map.get("content-type");   // json
map.get("CONTENT-TYPE");   // json
map.keySet();              // ["Content-Type"]  原大小写 + 插入顺序
```

---

### 6  一句话速记

> **LinkedCaseInsensitiveMap = “LinkedHashMap + 小写索引表”：保留插入顺序和原 key 大小写，却支持任意大小写读写，专为 HTTP Header、MyBatis、配置项等大小写不敏感场景设计。**