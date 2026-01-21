/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Default object instantiation strategy for use in BeanFactories.
 *
 * <p>Uses CGLIB to generate subclasses dynamically if methods need to be
 * overridden by the container to implement <em>Method Injection</em>.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.1
 *
 * `CglibSubclassingInstantiationStrategy` 是 Spring 框架中用于通过 CGLIB 库动态生成子类以实现方法注入的策略类。以下是对 PASSTHROUGH、LOOKUP_OVERRIDE 和 METHOD_REPLACER 这三个字段所代表场景的详细解释：
 *
 * ### PASSTHROUGH
 * - **含义**：表示 CGLIB 回调数组中的索引位置，用于直接调用原始类的方法。
 * - **场景**：当不需要对方法调用进行任何增强或拦截时，即子类不会覆盖原始类的方法。这种情况下，调用方法时会直接执行原始类中的实现，不会经过任何额外的处理逻辑。
 *
 * ### LOOKUP_OVERRIDE
 * - **含义**：表示 CGLIB 回调数组中的索引位置，用于覆盖方法以提供方法查找功能。
 * - **场景**：与 Spring 中的 `lookup-method` 配置相关。当需要在运行时动态查找某个方法的实现时，可以使用 `lookup-method` 指定一个方法来替代原始方法的实现，通常用于实现类似抽象方法的动态查找逻辑。
 *
 * ### METHOD_REPLACER
 * - **含义**：表示 CGLIB 回调数组中的索引位置，用于通过通用的方法替换功能来覆盖方法。
 * - **场景**：与 Spring 中的 `replaced-method` 配置相关。当需要完全替换某个方法的实现时，可以使用 `replaced-method` 指定一个新的方法来替代原始方法，这允许开发者提供自定义的方法实现来代替原有的方法逻辑。
 *
 * 这些字段定义了在使用 CGLIB 动态生成子类时，如何处理方法调用的三种不同策略。通过这些策略，Spring 能够灵活地实现方法注入和动态方法替换等功能。
 */
public class CglibSubclassingInstantiationStrategy extends SimpleInstantiationStrategy {

	/**
	 * Index in the CGLIB callback array for passthrough(穿透) behavior,
	 * in which case the subclass won't override the original class.
	 */
	private static final int PASSTHROUGH = 0;

	/**
	 * Index in the CGLIB callback array for a method that should
	 * be overridden to provide <em>method lookup</em>.
	 */
	private static final int LOOKUP_OVERRIDE = 1;

	/**
	 * Index in the CGLIB callback array for a method that should
	 * be overridden using generic <em>method replacer</em> functionality.
	 */
	private static final int METHOD_REPLACER = 2;


	@Override
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		return instantiateWithMethodInjection(bd, beanName, owner, null);
	}

	@Override
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			@Nullable Constructor<?> ctor, Object... args) {

		// Must generate CGLIB subclass...
		return new CglibSubclassCreator(bd, owner).instantiate(ctor, args);
	}


	/**
	 * An inner class created for historical reasons to avoid external CGLIB dependency
	 * in Spring versions earlier than 3.2.
	 */
	private static class CglibSubclassCreator {

		private static final Class<?>[] CALLBACK_TYPES = new Class<?>[]
				{NoOp.class, LookupOverrideMethodInterceptor.class, ReplaceOverrideMethodInterceptor.class};

		private final RootBeanDefinition beanDefinition;

		private final BeanFactory owner;

		CglibSubclassCreator(RootBeanDefinition beanDefinition, BeanFactory owner) {
			this.beanDefinition = beanDefinition;
			this.owner = owner;
		}

		/**
		 * Create a new instance of a dynamically generated subclass implementing the
		 * required lookups.
		 * @param ctor constructor to use. If this is {@code null}, use the
		 * no-arg constructor (no parameterization, or Setter Injection)
		 * @param args arguments to use for the constructor.
		 * Ignored if the {@code ctor} parameter is {@code null}.
		 * @return new instance of the dynamically generated subclass
		 */
		public Object instantiate(@Nullable Constructor<?> ctor, Object... args) {
			Class<?> subclass = createEnhancedSubclass(this.beanDefinition);
			Object instance;
			if (ctor == null) {
				instance = BeanUtils.instantiateClass(subclass);
			}
			else {
				try {
					Constructor<?> enhancedSubclassConstructor = subclass.getConstructor(ctor.getParameterTypes());
					instance = enhancedSubclassConstructor.newInstance(args);
				}
				catch (Exception ex) {
					throw new BeanInstantiationException(this.beanDefinition.getBeanClass(),
							"Failed to invoke constructor for CGLIB enhanced subclass [" + subclass.getName() + "]", ex);
				}
			}
			// SPR-10785: set callbacks directly on the instance instead of in the
			// enhanced class (via the Enhancer) in order to avoid memory leaks.
			Factory factory = (Factory) instance;
			factory.setCallbacks(new Callback[] {
					/**
					 * 作用: 这是一个空操作回调，用于处理不需要任何特殊逻辑的方法调用。它是一个占位符，表示不需要进行任何拦截或增强。
					 * 使用场景: 当方法不需要被拦截或增强时，使用 NoOp.INSTANCE 可以避免不必要的开销。
					*/
					NoOp.INSTANCE,
					/**
					 * 作用: 这个拦截器用于处理 lookup-method 配置。它会拦截指定的方法调用，并在运行时动态查找目标方法的实现。
					 * 使用场景: 当需要在运行时动态查找某个方法的实现（例如，动态获取原型作用域的 Bean）时，使用这个拦截器。
					*/
					new LookupOverrideMethodInterceptor(this.beanDefinition, this.owner),
					/**
					 * 作用: 这个拦截器用于处理 replaced-method 配置。它会拦截指定的方法调用，并使用自定义的实现来替换原始方法的逻辑。
					 * 使用场景: 当需要完全替换某个方法的实现时，使用这个拦截器。这在需要修改第三方库中某个类的方法行为时非常有用。
					 */
					new ReplaceOverrideMethodInterceptor(this.beanDefinition, this.owner)});
			return instance;
		}

		/**
		 * Create an enhanced subclass of the bean class for the provided bean
		 * definition, using CGLIB.
		 */
		private Class<?> createEnhancedSubclass(RootBeanDefinition beanDefinition) {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(beanDefinition.getBeanClass());
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			/**
			 * 这段代码的作用是确保 CGLIB 在生成代理类时能够使用正确的类加载器。具体来说：
			 *
			 * ### 代码分析
			 *
			 * 1. **检查 `this.owner` 的类型**：
			 *    - 如果 `this.owner` 是 `ConfigurableBeanFactory` 的实例，说明当前环境是一个可配置的 bean 工厂（通常是 Spring 的应用上下文）。
			 *
			 * 2. **获取类加载器**：
			 *    - 从 `ConfigurableBeanFactory` 中获取 bean 类加载器 (`getBeanClassLoader()` 方法返回 Spring 容器使用的类加载器)。
			 *
			 * 3. **设置 CGLIB 的生成策略**：
			 *    - 创建一个 `ClassLoaderAwareGeneratorStrategy` 实例，并将获取的类加载器传递给它。
			 *    - 将这个策略设置到 `enhancer`（CGLIB 的 `Enhancer` 类的实例）中，以确保在生成代理类时使用正确的类加载器。
			 *
			 * ### 作用和意义
			 *
			 * - **确保类加载器一致性**：
			 *   - 在 Spring 应用中，确保 CGLIB 生成的代理类使用与 Spring 容器相同的类加载器非常重要。这可以避免类加载器不一致导致的问题，例如类找不到 (`ClassNotFoundException`) 或类不兼容等。
			 *
			 * - **灵活性和可配置性**：
			 *   - 通过使用 `ConfigurableBeanFactory` 提供的类加载器，代码能够适应不同的部署环境和类加载器配置。这对于复杂的应用部署场景尤其重要。
			 *
			 * - **整合 CGLIB 和 Spring**：
			 *   - 这段代码展示了 Spring 如何整合 CGLIB 库来实现 AOP（面向切面编程）等功能。Spring 广泛使用 CGLIB 来动态生成类的子类，以便在不修改原始类代码的情况下添加额外的功能（如事务管理、性能监控等）。
			 *
			 * ### 总结
			 *
			 * 这段代码确保在使用 CGLIB 动态生成代理类时，代理类能够正确地使用 Spring 容器的类加载器。这种做法保证了 Spring 应用的类加载器环境的一致性，避免了潜在的类加载问题。同时，这也体现了 Spring 框架在整合第三方库（如 CGLIB）时的细节处理和灵活性。
			 */
			if (this.owner instanceof ConfigurableBeanFactory) {
				ClassLoader cl = ((ConfigurableBeanFactory) this.owner).getBeanClassLoader();
				enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(cl));
			}
			enhancer.setCallbackFilter(new MethodOverrideCallbackFilter(beanDefinition));
			enhancer.setCallbackTypes(CALLBACK_TYPES);
			return enhancer.createClass();
		}
	}


	/**
	 * Class providing hashCode and equals methods required by CGLIB to
	 * ensure that CGLIB doesn't generate a distinct class per bean.
	 * Identity is based on class and bean definition.
	 */
	private static class CglibIdentitySupport {

		private final RootBeanDefinition beanDefinition;

		public CglibIdentitySupport(RootBeanDefinition beanDefinition) {
			this.beanDefinition = beanDefinition;
		}

		public RootBeanDefinition getBeanDefinition() {
			return this.beanDefinition;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (other != null && getClass() == other.getClass() &&
					this.beanDefinition.equals(((CglibIdentitySupport) other).beanDefinition));
		}

		@Override
		public int hashCode() {
			return this.beanDefinition.hashCode();
		}
	}


	/**
	 * CGLIB callback for filtering method interception behavior.
	 */
	private static class MethodOverrideCallbackFilter extends CglibIdentitySupport implements CallbackFilter {

		private static final Log logger = LogFactory.getLog(MethodOverrideCallbackFilter.class);

		public MethodOverrideCallbackFilter(RootBeanDefinition beanDefinition) {
			super(beanDefinition);
		}

		@Override
		public int accept(Method method) {
			MethodOverride methodOverride = getBeanDefinition().getMethodOverrides().getOverride(method);
			if (logger.isTraceEnabled()) {
				logger.trace("MethodOverride for " + method + ": " + methodOverride);
			}
			if (methodOverride == null) {
				return PASSTHROUGH;
			}
			else if (methodOverride instanceof LookupOverride) {
				return LOOKUP_OVERRIDE;
			}
			else if (methodOverride instanceof ReplaceOverride) {
				return METHOD_REPLACER;
			}
			throw new UnsupportedOperationException("Unexpected MethodOverride subclass: " +
					methodOverride.getClass().getName());
		}
	}


	/**
	 * CGLIB MethodInterceptor to override methods, replacing them with an
	 * implementation that returns a bean looked up in the container.
	 */
	private static class LookupOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

		private final BeanFactory owner;

		public LookupOverrideMethodInterceptor(RootBeanDefinition beanDefinition, BeanFactory owner) {
			super(beanDefinition);
			this.owner = owner;
		}

		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
			// Cast is safe, as CallbackFilter filters are used selectively.
			LookupOverride lo = (LookupOverride) getBeanDefinition().getMethodOverrides().getOverride(method);
			Assert.state(lo != null, "LookupOverride not found");
			Object[] argsToUse = (args.length > 0 ? args : null);  // if no-arg, don't insist on args at all
			//通过 BeanFactory 来获取对应的bean
			if (StringUtils.hasText(lo.getBeanName())) {
				Object bean = (argsToUse != null ? this.owner.getBean(lo.getBeanName(), argsToUse) :
						this.owner.getBean(lo.getBeanName()));
				// Detect package-protected NullBean instance through equals(null) check
				return (bean.equals(null) ? null : bean);
			}
			else {
				return (argsToUse != null ? this.owner.getBean(method.getReturnType(), argsToUse) :
						this.owner.getBean(method.getReturnType()));
			}
		}
	}


	/**
	 * CGLIB MethodInterceptor to override methods, replacing them with a call
	 * to a generic MethodReplacer.
	 */
	private static class ReplaceOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

		private final BeanFactory owner;

		public ReplaceOverrideMethodInterceptor(RootBeanDefinition beanDefinition, BeanFactory owner) {
			super(beanDefinition);
			this.owner = owner;
		}

		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
			ReplaceOverride ro = (ReplaceOverride) getBeanDefinition().getMethodOverrides().getOverride(method);
			Assert.state(ro != null, "ReplaceOverride not found");
			// TODO could cache if a singleton for minor performance optimization
			// 直接根据 methodReplacerBeanName 获取到对应的bean，调用对应的方法 完整覆盖之前的实现
			MethodReplacer mr = this.owner.getBean(ro.getMethodReplacerBeanName(), MethodReplacer.class);
			return mr.reimplement(obj, method, args);
		}
	}

}
