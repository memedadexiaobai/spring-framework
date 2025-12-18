FlashMap 是 Spring MVC 专为 **重定向（Redirect）** 场景提供的 **一次性** 数据传递容器，解决 **POST-Redirect-GET** 模式下 **参数无法随重定向携带** 的问题。

---

### 1. 核心定位
| 问题 | 传统做法缺陷 | FlashMap 方案 |
| ---- | ------------ | ------------- |
| URL 拼接参数 | 长度受限、暴露敏感数据 | **不写入 URL，后台临时存储** |
| Session 手动存取 | 需自己清理，易内存泄漏 | **自动保存、自动移除** |
| 写库 | 太重 | **纯内存，无 IO** |

---

### 2. 生命周期（一次请求链）
1. **请求 A**（POST）  
   → 把数据放进 **output FlashMap**  
   → 框架将其暂存（默认 **Session**）  
   → 发出 **Redirect**
2. **请求 B**（GET）  
   → 框架把同一份数据取出 → 放进 **Model**  
   → **立即从存储中删除**  
   → 页面可直接使用 EL 显示

---

### 3. 关键接口与实现
| 接口/类 | 职责 |
| -------- | ---- |
| `FlashMap` | 继承 `HashMap<String,Object>`，可存任意属性；支持 **目标路径 + 参数** 精准匹配 |
| `FlashMapManager` | 存/取/过期管理；默认实现 `SessionFlashMapManager` → **Session 存储** |
| `RequestContextUtils` | 工具类，手动获取 `input/output` FlashMap |
| `RedirectAttributes` | 控制器层 façade，**推荐使用**；`addFlashAttribute("key",value)` 即可 |

---

### 4. 代码示例
```java
@PostMapping("/order")
public String createOrder(Order order, RedirectAttributes ra) {
    Long id = service.save(order);
    ra.addFlashAttribute("msg", "订单已创建");   // ① 写入 output FlashMap
    return "redirect:/order/" + id;              // ② 重定向
}

@GetMapping("/order/{id}")
public String showOrder(@PathVariable Long id, Model model) {
    // ③ 框架自动把 FlashMap → Model，页面可直接 ${msg}
    return "orderDetail";
}
```
页面（orderDetail.jsp）：
```jsp
<p>${msg}</p>  <%-- 显示：订单已创建 --%>
```
刷新后消息消失——**一次性**。

---

### 5. 高级特性
- **精准匹配**：可设置目标路径、参数，避免并发请求误拿数据。
- **自动过期**：超过一定时间未被取用即被清理。
- **无 Session 时不创建**：未使用 FlashMap 不会主动新建会话。
- **集群注意**：默认基于 Session，分布式需自定义 `FlashMapManager`（Redis 等）。

---

### 6. 一句话总结
> **FlashMap = “重定向专用的一次性 Session”**：  
> **POST 后写进去，GET 后自动读并删除，解决 PRG 模式数据传递，无需 URL 暴露，无需手动清理。**