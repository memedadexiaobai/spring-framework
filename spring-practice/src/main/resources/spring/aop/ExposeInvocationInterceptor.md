`ExposeInvocationInterceptor.ADVISOR` 是一个特殊的拦截器，它允许在 Spring AOP 拦截器链中暴露当前的调用上下文，以便其他拦截器或目标对象可以访问到当前的拦截器链和调用信息。

### 为什么要添加 `ExposeInvocationInterceptor.ADVISOR`

- **动态修改切面逻辑**：在某些高级使用场景中，切面逻辑可能需要根据当前的调用上下文动态调整。通过 `ExposeInvocationInterceptor`，可以在拦截器链中获取当前的调用信息（如方法名、参数等），从而实现动态调整切面逻辑。

- **调试和监控**：在调试或监控应用时，可能需要访问当前的拦截器链或方法调用信息。`ExposeInvocationInterceptor` 提供了这种访问能力，便于实现自定义的调试工具或监控功能。

- **与其他切面逻辑集成**：当有多个切面逻辑需要协同工作时，`ExposeInvocationInterceptor` 可以作为桥梁，使得不同切面之间可以共享调用上下文信息。

### 示例场景

假设我们有一个切面用于记录方法执行时间，同时还有一个切面用于验证方法参数。我们希望在执行时间切面中访问参数验证切面的结果，以决定是否记录执行时间。通过 `ExposeInvocationInterceptor`，我们可以在执行时间切面中获取到参数验证切面的执行信息。

```java
@Aspect
public class TimingAspect {
    @Around("execution(* com.example.service.*.*(..))")
    public Object recordExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 执行方法
        Object result = joinPoint.proceed();

        stopWatch.stop();
        // 通过 ExposeInvocationInterceptor 获取当前调用上下文
        MethodInvocation invocation = ExposeInvocationInterceptor.getCurrentInvocation();
        if (invocation != null) {
            // 访问参数验证切面的结果
            Object validationResult = invocation.getArguments()[0];
            if (validationResult instanceof Boolean && (Boolean) validationResult) {
                System.out.println("Method executed in " + stopWatch.getTotalTimeMillis() + " ms");
            }
        }
        return result;
    }
}
```

通过添加 `ExposeInvocationInterceptor.ADVISOR` 到拦截器链中，我们可以在切面逻辑中访问当前的调用上下文，从而实现更灵活和强大的切面功能。