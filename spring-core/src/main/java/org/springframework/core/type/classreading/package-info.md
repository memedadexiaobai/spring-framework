`package-info.java` 是 **专门为“包”服务的元数据文件**，文件名固定、**必须放在对应包目录下**，编译后生成特殊的 `package-info.class`，但它 **不包含任何可执行代码**，只做三件事：

---

### 1  给整个包写 **Javadoc 总览**
```java
/**
 * 本包提供订单域相关领域模型与服务。
 * <p>所有实现线程安全，不可变对象可直接暴露。
 */
package com.example.order;
```
- Maven/Gradle 生成 Javadoc 时，会在包首页显示这段描述。  
- 替代以前的 `package.html`。

---

### 2  统一加 **包级注解**（最常见用法）
```java
@NonNullApi                    // 整个包默认参数/返回不允许 null
@NonNullFields                 // 整个包字段默认非 null
package com.example.order;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;
```
- Spring、JSR-305、Error-Prone、SpotBugs 等工具都会读取这些注解。  
- 避免在每个类上重复写 `@NonNull/@ParametersAreNonnullByDefault`。

---

### 3  声明 **包访问控制 / 模块信息**
- 在 Java 9 + 模块系统里，可在同一个文件顶部写 `module` 语句（少见）。  
- 也可配合 `opens`/`exports` 做细粒度模块导出。

---

### 4  规则速记
| 规则 | 说明 |
|---|---|
| 文件名 | 固定为 `package-info.java` |
| 位置 | 必须与目标包同目录 |
| 类名 | 无 `public class`，仅 `package` 语句 + 注解/注释 |
| 编译产物 | `package-info.class`（几乎为空，仅保留注解与常量池） |

---

### 一句话总结  
> `package-info.java` 就是“包的 **README + 注解总开关**”，用来写包级 Javadoc 和一次性声明对整个包生效的注解。