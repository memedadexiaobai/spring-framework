AOP涉及三个关键的接口，如下：
1. Advisor：英文译为顾问，导师，我觉得不妨理解为顾问，而顾问的作用是什么呢？提建议，因此他的属性包含了建议：Advice
2. Advice：英文译为建议，这里只是一个标记性接口
3. Pointcut：英文译为切入点，也就是说建议提给谁，落地到代码的层面，就是类级别的 ClassFilter 和 方法级别的 MethodMatcher 符合条件的给他们提建议
------
# Advisor
```java
public interface Advisor {
    
	Advice EMPTY_ADVICE = new Advice() {};
    
	Advice getAdvice();
    
	boolean isPerInstance();

}
```
![Advisor](/Users/a58/github_workspace/spring-framework/spring-practice/src/main/resources/spring/aop/img/Advisor.png)
# Advice
```java
public interface Advice {
    
}
```
![Advice](/Users/a58/github_workspace/spring-framework/spring-practice/src/main/resources/spring/aop/img/Advice.png)
# Pointcut
```java
public interface Pointcut {
    
	ClassFilter getClassFilter();
    
	MethodMatcher getMethodMatcher();
    
	Pointcut TRUE = TruePointcut.INSTANCE;

}

```
![Pointcut](/Users/a58/github_workspace/spring-framework/spring-practice/src/main/resources/spring/aop/img/Pointcut.png)