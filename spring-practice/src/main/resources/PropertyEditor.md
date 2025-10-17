`PropertyEditor` 是 JavaBean 规范提供的 **“属性编辑器”** 接口，  
**把字符串 → 任意类型（或反向）的转换逻辑封装成一个可复用组件**，  
供 **可视化设计器、Spring MVC、MyBatis、JSP、JavaFX** 等框架 **自动完成赋值、校验、格式化**。

---

### 1  接口定义（简化）

```java
public interface PropertyEditor {
    void setValue(Object value);     // 内部保存值
    Object getValue();               // 拿回转换后对象
    void setAsText(String text);     // 核心：字符串 → 对象
    String getAsText();              // 对象 → 字符串
    boolean supportsCustomEditor();  // 是否提供 GUI 面板
    Component getCustomEditor();     // 返回 Swing 面板（可选）
    // 还有 add/remove PropertyChangeListener
}
```

---

### 2  内置实现（JDK 已备好）

| 目标类型 | 实现类 | 示例字符串 |
|---|---|---|
| `boolean/Boolean` | `BooleanEditor` | `"true"` ↔ `true` |
| `int/Integer` | `IntegerEditor` | `"123"` ↔ `123` |
| `float/Float` | `FloatEditor` | `"3.14"` ↔ `3.14` |
| `Color` | `ColorEditor` | `"#FF0000"` ↔ `Color.RED` |
| `Font` | `FontEditor` | `"Arial-BOLD-14"` ↔ `Font` |
| `String[]` | `StringArrayEditor` | `"a,b,c"` ↔ `new String[]{"a","b","c"}` |

---

### 3  自定义编辑器：两步搞定

#### ① 实现接口（或继承 `PropertyEditorSupport`）

```java
public class LocalDateEditor extends PropertyEditorSupport {
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void setAsText(String text) {
        LocalDate date = LocalDate.parse(text, fmt);
        setValue(date);
    }

    @Override
    public String getAsText() {
        LocalDate value = (LocalDate) getValue();
        return value == null ? "" : value.format(fmt);
    }
}
```

#### ② 注册到全局仓库（或局部注册）

```java
PropertyEditorManager.registerEditor(LocalDate.class, LocalDateEditor.class);
```

---

### 4  框架集成场景

| 框架 | 用途 |
|---|---|
| **Spring MVC** | `@InitBinder` + `WebDataBinder.registerCustomEditor` 把表单字符串 → 日期、枚举、自定义类型 |
| **Spring Boot** | `application.properties` 字符串 → `Duration`、`DataSize`、`LocalDate` 等 |
| **MyBatis** | `TypeHandler` 底层可复用 `PropertyEditor` 做字符串 ↔ Java 类型转换 |
| **JSP `<jsp:setProperty>`** | 自动调用对应编辑器完成赋值 |
| **JavaFX Scene Builder** | 属性面板把文本 → Color/Font/Insets |

---

### 5  一句话速记

> **PropertyEditor = “字符串到任意类型的转换插头”：JavaBean 规范自带，框架用它一键把文本配置、表单输入变成真正的 Java 对象。**