`LinkedMultiValueMap` 是 Spring Framework 提供的 **“一键存多个值”** 的专用 Map 实现，  
**在 `LinkedHashMap<K, List<V>>` 基础上再做一层“列表自动创建/追加”包装**，  
**保持插入顺序，同时让 `put`/`add` 操作像单值 Map 一样简单**，  
**广泛用于 HTTP 参数、请求头、URI 查询串、MyBatis 批量参数等“一对多”场景。**

---

### 1  核心能力（一句话）

> **“一个 key 对应多个值，List 自动创建/追加，顺序保留，API 极简。”**

---

### 2  与 `Map<K,List<V>>` 的对比

| 操作 | 原生 `LinkedHashMap<K,List<V>>` | `LinkedMultiValueMap<K,V>` |
|---|---|---|
| 首次添加 | 先 `computeIfAbsent` 再 `add` | **一行 `add(key, value)` 搞定** |
| 追加 | 手动 `get` + `add` | `add(key, value)` 自动追加 |
| 单值添加 | 需 `Collections.singletonList` | `set(key, value)` 覆盖整 List |
| 空值安全 | 可能 NPE | **永不抛 NPE**（List 自动 new） |
| 遍历顺序 | 需要两层循环 | `forEach((k, vs) -> ...)` 一次到位 |

---

### 3  常用 API（够用清单）

| 方法 | 说明 |
|---|---|
| `add(K key, V value)` | 追加一个值（List 不存在则自动 new） |
| `set(K key, V value)` | **覆盖**为单值 List |
| `addAll(K key, List<? extends V> values)` | 批量追加 |
| `getFirst(K key)` | 快速拿第一个值（常用于“只取一个”场景） |
| `toSingleValueMap()` | 转成普通 `Map<K,V>`（每个 key 只保留 **第一个**值） |
| `forEach(BiConsumer<K, List<V>> action)` | 顺序遍历 key → values |

---

### 4  代码示例（一行即可）

```java
MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

params.add("id", "1");
params.add("id", "2");      // key="id" 现在有两个值
params.set("name", "Tom");  // 覆盖整 List

System.out.println(params); // {id=[1, 2], name=[Tom]}
System.out.println(params.getFirst("id")); // 1
System.out.println(params.toSingleValueMap()); // {id=1, name=Tom}
```

---

### 5  典型使用场景

| 场景 | 用途 |
|---|---|
| **Spring MVC / WebFlux** | 注入 **HTTP 查询参数** (`?id=1&id=2`) |
| **HttpHeaders** | 一个头多个值 (`Accept: application/json, text/plain`) |
| **UriComponentsBuilder** | 构建 **RestTemplate 请求 URL** |
| **MyBatis 批量参数** | `foreach` 标签直接传 `List<V>` |
| **OAuth2 / JWT  claims** | 一个 claim 允许多值 |

---

### 6  线程安全与性能

- **非线程安全**（同 `LinkedHashMap`）；
- 若需并发，使用 `ConcurrentHashMap<K,List<V>>` 或 `ConcurrentMultiValueMap`（Guava）。
- 插入/查询性能 ≈ `LinkedHashMap` + 一次 `ArrayList` 创建。

---

### 7  一句话速记

> **LinkedMultiValueMap = “LinkedHashMap 的 List 值自动包装器”：一个 key 存多个值，API 像单值 Map 一样简单，顺序保留，专为 HTTP 参数、请求头、批量数据设计。**