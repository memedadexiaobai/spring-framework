Java 语言规范（Java Language Specification，JLS）是 **Java 语法的“官方宪法”**，由 Oracle（原 Sun）发布，**所有 Java 实现（编译器、JVM、IDE）都必须遵守**。  
2025 年最新有效版本为 **Java SE 21**（对应 JSR 396），涵盖语法、语义、类型系统、并发、模块、模式匹配等全部细节。

---

### 1  规范组成（2025 最新）

| 文档 | 内容 | 获取地址 |
|---|---|---|
| **The Java Language Specification（JLS）** | 语法、语义、类型、并发、模块、模式匹配等 | https://docs.oracle.com/javase/specs/jls/se21/html/  |
| **The Java Virtual Machine Specification（JVMS）** | 字节码、类文件格式、指令集、运行时 | 同目录下 `jvms/se21` |
| **Java API Specification（Javadoc）** | 标准库类与方法说明 | https://docs.oracle.com/en/java/javase/21/docs/api/  |
| **Java SE Documentation Guides** | 新特性总结、迁移指南、工具使用 | 同目录 `guides/` |

---

### 2  规范章节速览（以 Java 21 为例）

| 章节 | 关键词 |
|---|---|
| 1–4 | 词法、类型、值、变量、作用域 |
| 5–7 | 转换、名字、包、模块 |
| 8–9 | 类、接口、嵌套、继承、重写 |
| 10–11 | 数组、异常、执行模型 |
| 12–14 | 兼容性、二进制格式、块与语句 |
| 15 | **表达式**（含模式匹配、record 解构） |
| 16 | ** definite assignment**（确定赋值） |
| 17 | 线程与锁（Java 内存模型基础） |
| 18 | 语法汇总（BNF） |

---

### 3  中文版与阅读建议

- **官方英文版**实时更新，**无 Oracle 中文翻译**；  
  社区译本（Java 8/11）可参考，但 **建议直接读英文原版** 避免歧义。
- **阅读顺序**：先通读 **Chapter 15 表达式** 与 **Chapter 8 类/接口**，再按需跳章节；遇到编译器报错可直接搜 JLS 条款号（如 *§15.12.2.5*）。

---

### 4  一句话速记

> **JLS = Java 语法宪法；2025 最新 Java 21 规范在线可查，章节 15、8、17 是高频考点，英文原版最权威。** 