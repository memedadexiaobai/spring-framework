`@args` 是 Spring AOP 中的另一个切点表达式，用于匹配那些方法参数满足特定条件的方法。它关注的是方法参数的注解或类型。

### `@args`

- **匹配参数满足条件的方法**：`@args` 用于匹配那些方法参数满足特定条件的方法。这个条件通常是参数被某个注解标记，或者参数类型符合某个条件。
- **作用在参数级别**：它关注的是方法参数的注解或类型。
- **用法示例**：
  ```java
  @Aspect
  public class ArgsAspect {
      @Around("@args(com.example.MyParameterAnnotation)")
      public Object interceptMethodWithAnnotatedArgs(ProceedingJoinPoint pjp) throws Throwable {
          System.out.println("Method with parameter annotated by MyParameterAnnotation called");
          return pjp.proceed();
      }
  }
  ```
  在这个例子中，切面会拦截所有至少有一个参数被 `@MyParameterAnnotation` 注解的方法。

### 总结

- **`@annotation`**：匹配带有特定注解的方法。
- **`@within`**：匹配定义在带有特定注解的类中的所有方法。
- **`@args`**：匹配参数满足特定条件的方法（如参数被某个注解标记）。

选择哪一个切点表达式取决于你的需求：如果需要对带有特定注解的方法进行增强，使用 `@annotation`；如果需要对整个类中的所有方法进行增强，使用 `@within`；如果需要对参数满足特定条件的方法进行增强，使用 `@args`。