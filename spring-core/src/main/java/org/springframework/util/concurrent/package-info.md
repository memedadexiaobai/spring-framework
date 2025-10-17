这张图里列出的全部是 **Spring Framework 对“异步计算结果”——ListenableFuture 生态** 的 **类型、适配器与回调机制** 的 **全家桶目录**；  
可以把它理解为 **“JDK CompletableFuture / Reactor Mono 的 Spring 版扩展包”**，核心目标：

> **在 JDK 8 之前（或不想强制依赖 Reactor）的场景下，仍能统一使用“可监听、可链式、可回调”的异步抽象，同时与 CompletableFuture、Mono 无缝互转。**

---

### 1  核心接口与实现（★ 必须记住）

| 类 / 接口 | 作用 |
|---|---|
| **ListenableFuture** | Spring 自研的“可监听 Future”，**addCallback()** 实现 **非阻塞回调**；**done → 回调链** 模式 |
| **SettableListenableFuture** | 对标 `CompletableFuture` 的 **手动设置结果/异常** 实现；**set()/setException()** 即可唤醒回调 |
| **ListenableFutureTask** | 把 `Callable/Runnable` 包装成 **ListenableFuture**，线程池执行后自动回调 |
| **ListenableFutureCallbackRegistry** | **回调链注册中心**；支持 **SuccessCallback / FailureCallback** 两种形态 |
| **SuccessCallback / FailureCallback** | 函数式接口，**泛型隔离成功与异常路径**（早于 JDK 8 `BiConsumer`） |

---

### 2  适配器（Adapter）——与异步生态互通

| 适配器 | 用途 |
|---|---|
| **CompletableToListenableFutureAdapter** | **CompletableFuture → ListenableFuture**（老代码只认 Spring 接口） |
| **MonoToListenableFutureAdapter** | **Reactor Mono → ListenableFuture**（WebFlux 调用老链路） |
| **ListenableFutureAdapter** | **ListenableFuture → CompletableFuture**（反向适配） |
| **DelegatingCompletableFuture** | **同时实现 ListenableFuture + CompletableFuture** 接口，**双向兼容** |

---

### 3  回调注册方式（两种风格）

| 风格 | 示例 |
|---|---|
| **接口回调** | `future.addCallback(new SuccessCallback<T>(){...}, new FailureCallback(){...})` |
| **函数式** | `future.onSuccess(v -> log.info("ok")) .onFailure(ex -> log.error("fail", ex))` |

---

### 4  使用场景（Spring 内部高频）

| 场景 | 用法 |
|---|---|
| **@Async 方法** | Spring 默认返回 `ListenableFuture`（JDK 7/8 时代） |
| **RestTemplate 异步** | `AsyncRestTemplate` 所有方法返回 `ListenableFuture<ResponseEntity>` |
| **消息监听器** | `MessageListenerContainer` 用 `SettableListenableFuture` 实现异步确认 |
| **与 Reactor 互操作** | `Mono.fromFuture(listenableFuture)` 或反向 `MonoToListenableFutureAdapter` |

---

### 5  一句话速记

> **“ListenableFuture 是 Spring 的‘可监听 Future’全家桶；通过 SettableListenableFuture、各种 Adapter 与回调接口，让老代码也能享受非阻塞链式调用，同时与 CompletableFuture / Mono 无缝互转。”**