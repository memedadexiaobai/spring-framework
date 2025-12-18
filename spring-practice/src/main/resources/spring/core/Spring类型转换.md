Spring 的类型转换 **历史包袱** 确实一层套一层，但只要抓住 **“三代接口 + 两条主线”** 就能一眼看穿：

---

### 一、三代核心接口（时间轴）

| 世代 | 接口/类 | 职责 | 诞生版本 | 备注 |
| ---- | ------- | ---- | -------- | ---- |
| **1. 远古** | `java.beans.PropertyEditor` | JDK 原生 **字符串↔对象** 转换 | Java 1.1 | 只能 **String ↔ Object**；线程不安全 |
| **2. 过渡** | `PropertyEditorRegistry` | **注册 & 查找** 一批 `PropertyEditor` | Spring 1.0 | 类似“转换器工厂”，无转换行为 |
| **3. 现代** | `Converter<S,T>` 家族 | **任意类型 ↔ 任意类型** 转换 | Spring 3.0 | 线程安全、函数式、支持泛型 |

---

### 二、两条主线（Spring 内部同时存在）

#### ✅ **老线：PropertyEditor 路线**（兼容 JDK / 历史代码）
```
PropertyEditor
   ↑ implements
StringToNumberEditor / DateEditor / ...（用户自定义）
   ↑ register
PropertyEditorRegistry
   ↑ extends
BeanWrapperImpl（Bean 属性注入时查找编辑器）
   ↑ 辅助
PropertyEditorRegistrar（批量注册器）
```
- **使用场景**：`<property name="date" value="2020-01-01"/>`、MVC 表单提交、XML 配置。
- **缺点**：只能 `String → Object`，线程不安全，需手动注册。

#### ✅ **新线：Converter 路线**（推荐）
```
Converter<S,T>
   ↑ implements
StringToInteger / StringToDate / ...（用户自定义）
   ↑ add
ConverterRegistry
   ↑ extends
ConversionService（统一门面）
   ↑ 默认实现
DefaultConversionService / GenericConversionService
```
- **使用场景**：`@Value("${port}") Integer port`、SpEL、`@PathVariable`、`@RequestParam`、注解驱动。
- **优点**：任意类型互转、线程安全、函数式、可组合。

---

### 三、角色对照（一张表记住）

| 角色 | 老线（PropertyEditor） | 新线（Converter） |
| ---- | ----------------------- | ------------------ |
| **转换器** | `PropertyEditor` | `Converter<S,T>` |
| **注册表** | `PropertyEditorRegistry` | `ConverterRegistry` |
| **门面服务** | 无（直接查表） | `ConversionService` |
| **批量注册器** | `PropertyEditorRegistrar` | `ConverterRegistry#addConverter` |
| **默认实例** | `BeanWrapperImpl` | `DefaultConversionService` |

---

### 四、Spring Boot 默认行为

- **同时支持两条线**，但**优先走 Converter**；找不到才回退到 PropertyEditor。
- 默认已注册 **70+ 内置 Converter**（String→Integer、String→Duration、String→DataSize…）。
- 用户扩展只需：
```java
@Configuration
public class MyConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToMyEntityConverter());
    }
}
```

---

### 五、一句话总结

> **“PropertyEditor 是老式字符串编辑器，PropertyEditorRegistry 只是它的‘注册表’；Spring 3 以后用线程安全的 Converter/ConversionService 统一类型转换，两条路并存但新线优先，注册方式从 `PropertyEditorRegistrar` 改为 `ConverterRegistry.addConverter` 即可。”**