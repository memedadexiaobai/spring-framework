AutoPopulatingList 是 **Spring 框架提供的“懒填充”列表包装器**，核心作用是：

> **当调用 `get(index)` 发现该下标元素为 `null` 时，自动实例化一个对象并放入列表，从而避免手动 `new` 或 `add`，彻底解决 Spring MVC 表单 **动态行绑定** 时出现的 `IndexOutOfBoundsException` 与空指针问题。**

---

### ✅ 一句话使用场景

> **“前端动态添加/删除表格行，后端用 List 接收，行数不确定且可能出现空洞下标”** —— 这就是 AutoPopulatingList 的主场。

---

### ✅ 经典场景举例（来自 2024-2025 实战资料）

| 场景 | 说明 | 来源 |
|---|---|---|
| **订单明细行** | 用户可 **动态添加/删除**订单条目，前端只传存在的行，后端 `List<OrderLine>` 若用普通 `ArrayList` 会下标越界；AutoPopulatingList 自动补空对象，再交由 JSR303 校验 |  |
| **问卷/试卷动态题** | 题目数量不定，前端按 `questions[2].content` 提交，中间缺题；AutoPopulatingList 自动实例化 `Question` 对象，保证绑定成功 |  |
| **子表编辑页** | Grails / Spring Boot 后端，**一对多关系**子表行增删改，前端只传“现有行”，后端用 AutoPopulatingList 接收，无需重排索引 |  |
| **JSR303 级联校验** | 集合元素需要 `@Valid` 校验，但前端可能传空行；AutoPopulatingList 先补空对象，再由校验器处理，避免 `null` 穿透 |  |

---

### ✅ 代码模板（直接复制可用）

```java
// ① 声明：用 AutoPopulatingList 代替普通 List
private List<OrderLine> lines = new AutoPopulatingList<>(OrderLine.class);

// ② 高级：自定义工厂（可传参、移除 null）
private List<OrderLine> lines = new AutoPopulatingList<>(index -> {
    lines.removeAll(Collections.singletonList(null)); // 清掉空洞
    return new OrderLine();                           // 按需创建
});
```

---

### ✅ 优点总结

1. **零前端改造**：前端随意增删行，**无需重排索引**；
2. **零后端越界**：`get(index)` 自动补对象，**无 `IndexOutOfBoundsException`**；
3. **兼容校验**：配合 `@Valid` 可对 **动态行内属性** 做 JSR303 校验；
4. **线程安全**：每个请求 new 一个 AutoPopulatingList，**无共享状态**。

---

### ✅ 一句话总结

> **“前端动态表格行增删，后端 List 接收怕越界？用 AutoPopulatingList —— 调用 `get(index)` 就自动 `new` 对象，空洞下标也能绑，校验不翻车。”**  
> 典型用于 **订单条目、问卷题目、子表编辑、级联校验** 等动态行场景 。