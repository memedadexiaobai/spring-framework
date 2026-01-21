`lookup-method` 和 `replaced-method` 是 Spring 框架中用于动态方法注入和替换的机制，下面是它们的使用场景和示例：

### `lookup-method`

* **使用场景**：适用于单例作用域的 Bean 需要获取原型作用域的 Bean 实例的场景。例如，在单例的 Service 层对象中，需要注入一个原型作用域的 DAO 层对象，此时就可以使用 `lookup-method` 来动态获取新的 DAO 层实例。
* **使用方法**：
    * 在 Bean 的类中定义一个方法用于获取原型 Bean，通常将该方法声明为抽象方法。
    * 在 Spring 配置文件中，使用 `<lookup-method>` 元素指定要查找的方法名和目标 Bean。
    * Spring 在运行时会动态生成该类的子类，并重写指定的方法，以返回目标 Bean 的新实例。

* **示例代码**：
    * 定义一个抽象方法获取原型 Bean：
      ```java
      public abstract class CommandManager {
        
        public Object process(Object commandState) {
            Command command = createCommand();
            command.setState(commandState);
            return command.execute();
         }
         @Lookup
         protected abstract Command createCommand();
      }
```
* Spring 配置文件中指定查找方法：
```xml
<bean id="commandManager" class="com.example.CommandManager">
    <lookup-method name="createCommand" bean="prototypeCommand"/>
</bean>
<bean id="prototypeCommand" class="com.example.PrototypeCommand" scope="prototype"/>
```

### `replaced-method`

* **使用场景**：适用于需要完全替换现有方法实现的场景。例如，当需要在运行时动态改变某个方法的行为，或者对第三方库中的某个类的方法行为进行修改时，可以使用 `replaced-method`。
* **使用方法**：
    * 定义一个实现 `MethodReplacer` 接口的类，并重写 `reimplement` 方法，提供新的方法实现逻辑。
    * 在 Spring 配置文件中，使用 `<replaced-method>` 元素指定要替换的方法名和替换器 Bean 的 ID。
    * 当调用目标 Bean 的被替换方法时，Spring 会使用替换器中的逻辑来执行方法。

* **示例代码**：
    * 定义替换器类：
      ```java
      public class ReplacementComputeValue implements MethodReplacer {
        @Override
        public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
            // 自定义方法实现
            return "Replaced Value";
        }
      }
```
* Spring 配置文件中指定替换方法：
      ```xml
<bean id="myBean" class="com.example.MyBean">
    <replaced-method name="computeValue" replacer="replacementComputeValue"/>
</bean>
<bean id="replacementComputeValue" class="com.example.ReplacementComputeValue"/>
```



`lookup-method` 主要用于解决单例 Bean 中获取原型 Bean 实例的问题，而 `replaced-method` 则用于动态替换方法的实现。在现代 Spring 开发中，虽然这些机制的使用频率有所下降，但它们在特定场景下仍然具有独特的价值。
https://blog.csdn.net/LightOfMiracle/article/details/74988243