在 CGLIB 动态代理中，`CallbackFilter` 和 `Callback` 的区别和联系如下：

### 区别

  * **功能不同** ：
    * `Callback` 是一个接口，它的实现类定义了方法被调用时的具体回调逻辑，比如 `MethodInterceptor` 就是一个典型的 `Callback` 实现，用于拦截方法调用并添加自定义逻辑。
    * `CallbackFilter` 主要用于确定每个方法调用对应的 `Callback`。它通过 `accept(Method method)` 方法返回一个整数索引，该索引表示方法调用时应使用的 `Callback` 数组中的位置。

  * **使用场景不同** ：
    * `Callback` 是执行具体逻辑的地方，用于实现各种不同的代理行为，如添加事务管理、日志记录、性能监控等功能。
    * `CallbackFilter` 用于对不同方法进行分类，根据方法的特征（如方法名、参数类型等）来选择不同的 `Callback`，从而实现对不同方法的差异化处理。

### 联系

  * **协同工作** ：在 CGLIB 动态代理中，`CallbackFilter` 和 `Callback` 通常一起使用。`CallbackFilter` 负责为每个方法选择合适的 `Callback`，而 `Callback` 则负责执行具体的代理逻辑。当通过 CGLIB 生成代理类并调用方法时，会先通过 `CallbackFilter` 确定使用哪个 `Callback`，然后将方法调用转发给对应的 `Callback` 处理。

  * **数组索引对应关系** ：`CallbackFilter` 的 `accept(Method method)` 方法返回的整数索引，对应的是传递给 `Enhancer` 的 `Callback` 数组中的索引位置。例如，如果 `CallbackFilter` 返回 0，则表示使用 `Callback` 数组中索引为 0 的回调逻辑来处理该方法调用。

总结来说，`CallbackFilter` 和 `Callback` 在 CGLIB 动态代理中分别承担着方法分类和具体逻辑处理的角色，它们相互配合，共同实现了灵活的动态代理功能。