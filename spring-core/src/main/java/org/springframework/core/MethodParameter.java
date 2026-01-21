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

package org.springframework.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import kotlin.Unit;
import kotlin.reflect.KFunction;
import kotlin.reflect.KParameter;
import kotlin.reflect.jvm.ReflectJvmMapping;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Helper class that encapsulates the specification of a method parameter, i.e. a {@link Method}
 * or {@link Constructor} plus a parameter index and a nested type index for a declared generic
 * type. Useful as a specification object to pass along.
 *
 * <p>As of 4.2, there is a {@link org.springframework.core.annotation.SynthesizingMethodParameter}
 * subclass available which synthesizes annotations with attribute aliases. That subclass is used
 * for web and message endpoint processing, in particular.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Andy Clement
 * @author Sam Brannen
 * @author Sebastien Deleuze
 * @author Phillip Webb
 * @since 2.0
 * @see org.springframework.core.annotation.SynthesizingMethodParameter
 */
public class MethodParameter {

	private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];


	private final Executable executable;

	private final int parameterIndex;

	@Nullable
	private volatile Parameter parameter;

	private int nestingLevel;

	/** Map from Integer level to Integer type index. */
	@Nullable
	Map<Integer, Integer> typeIndexesPerLevel;

	/** The containing class. Could also be supplied by overriding {@link #getContainingClass()} */
	@Nullable
	private volatile Class<?> containingClass;

	@Nullable
	private volatile Class<?> parameterType;

	@Nullable
	private volatile Type genericParameterType;

	@Nullable
	private volatile Annotation[] parameterAnnotations;

	@Nullable
	private volatile ParameterNameDiscoverer parameterNameDiscoverer;

	@Nullable
	private volatile String parameterName;

	@Nullable
	private volatile MethodParameter nestedMethodParameter;


	/**
	 * Create a new {@code MethodParameter} for the given method, with nesting level 1.
	 * @param method the Method to specify a parameter for
	 * @param parameterIndex the index of the parameter: -1 for the method
	 * return type; 0 for the first method parameter; 1 for the second method
	 * parameter, etc.
	 */
	public MethodParameter(Method method, int parameterIndex) {
		this(method, parameterIndex, 1);
	}

	/**
	 * Create a new {@code MethodParameter} for the given method.
	 * @param method the Method to specify a parameter for
	 * @param parameterIndex the index of the parameter: -1 for the method
	 * return type; 0 for the first method parameter; 1 for the second method
	 * parameter, etc.
	 * @param nestingLevel the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List)
	 */
	public MethodParameter(Method method, int parameterIndex, int nestingLevel) {
		Assert.notNull(method, "Method must not be null");
		this.executable = method;
		this.parameterIndex = validateIndex(method, parameterIndex);
		this.nestingLevel = nestingLevel;
	}

	/**
	 * Create a new MethodParameter for the given constructor, with nesting level 1.
	 * @param constructor the Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 */
	public MethodParameter(Constructor<?> constructor, int parameterIndex) {
		this(constructor, parameterIndex, 1);
	}

	/**
	 * Create a new MethodParameter for the given constructor.
	 * @param constructor the Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 * @param nestingLevel the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List)
	 */
	public MethodParameter(Constructor<?> constructor, int parameterIndex, int nestingLevel) {
		Assert.notNull(constructor, "Constructor must not be null");
		this.executable = constructor;
		this.parameterIndex = validateIndex(constructor, parameterIndex);
		this.nestingLevel = nestingLevel;
	}

	/**
	 * Internal constructor used to create a {@link MethodParameter} with a
	 * containing class already set.
	 * @param executable the Executable to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 * @param containingClass the containing class
	 * @since 5.2
	 */
	MethodParameter(Executable executable, int parameterIndex, @Nullable Class<?> containingClass) {
		Assert.notNull(executable, "Executable must not be null");
		this.executable = executable;
		this.parameterIndex = validateIndex(executable, parameterIndex);
		this.nestingLevel = 1;
		this.containingClass = containingClass;
	}

	/**
	 * Copy constructor, resulting in an independent MethodParameter object
	 * based on the same metadata and cache state that the original object was in.
	 * @param original the original MethodParameter object to copy from
	 */
	public MethodParameter(MethodParameter original) {
		Assert.notNull(original, "Original must not be null");
		this.executable = original.executable;
		this.parameterIndex = original.parameterIndex;
		this.parameter = original.parameter;
		this.nestingLevel = original.nestingLevel;
		this.typeIndexesPerLevel = original.typeIndexesPerLevel;
		this.containingClass = original.containingClass;
		this.parameterType = original.parameterType;
		this.genericParameterType = original.genericParameterType;
		this.parameterAnnotations = original.parameterAnnotations;
		this.parameterNameDiscoverer = original.parameterNameDiscoverer;
		this.parameterName = original.parameterName;
	}


	/**
	 * Return the wrapped Method, if any.
	 * <p>Note: Either Method or Constructor is available.
	 * @return the Method, or {@code null} if none
	 */
	@Nullable
	public Method getMethod() {
		return (this.executable instanceof Method ? (Method) this.executable : null);
	}

	/**
	 * Return the wrapped Constructor, if any.
	 * <p>Note: Either Method or Constructor is available.
	 * @return the Constructor, or {@code null} if none
	 */
	@Nullable
	public Constructor<?> getConstructor() {
		return (this.executable instanceof Constructor ? (Constructor<?>) this.executable : null);
	}

	/**
	 * Return the class that declares the underlying Method or Constructor.
	 */
	public Class<?> getDeclaringClass() {
		return this.executable.getDeclaringClass();
	}

	/**
	 * Return the wrapped member.
	 * @return the Method or Constructor as Member
	 */
	public Member getMember() {
		return this.executable;
	}

	/**
	 * Return the wrapped annotated element.
	 * <p>Note: This method exposes the annotations declared on the method/constructor
	 * itself (i.e. at the method/constructor level, not at the parameter level).
	 * @return the Method or Constructor as AnnotatedElement
	 */
	public AnnotatedElement getAnnotatedElement() {
		return this.executable;
	}

	/**
	 * Return the wrapped executable.
	 * @return the Method or Constructor as Executable
	 * @since 5.0
	 */
	public Executable getExecutable() {
		return this.executable;
	}

	/**
	 * Return the {@link Parameter} descriptor for method/constructor parameter.
	 * @since 5.0
	 */
	public Parameter getParameter() {
		if (this.parameterIndex < 0) {
			throw new IllegalStateException("Cannot retrieve Parameter descriptor for method return type");
		}
		// 默认创建的时候 parameter 是null
		Parameter parameter = this.parameter;
		if (parameter == null) {
			//根据 parameterIndex 直接找到对应的 Parameter
			parameter = getExecutable().getParameters()[this.parameterIndex];
			this.parameter = parameter;
		}
		return parameter;
	}

	/**
	 * Return the index of the method/constructor parameter.
	 * @return the parameter index (-1 in case of the return type)
	 */
	public int getParameterIndex() {
		return this.parameterIndex;
	}

	/**
	 * Increase this parameter's nesting level.
	 * @see #getNestingLevel()
	 * @deprecated since 5.2 in favor of {@link #nested(Integer)}
	 */
	@Deprecated
	public void increaseNestingLevel() {
		this.nestingLevel++;
	}

	/**
	 * Decrease this parameter's nesting level.
	 * @see #getNestingLevel()
	 * @deprecated since 5.2 in favor of retaining the original MethodParameter and
	 * using {@link #nested(Integer)} if nesting is required
	 */
	@Deprecated
	public void decreaseNestingLevel() {
		getTypeIndexesPerLevel().remove(this.nestingLevel);
		this.nestingLevel--;
	}

	/**
	 * Return the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List).
	 */
	public int getNestingLevel() {
		return this.nestingLevel;
	}

	/**
	 * Return a variant of this {@code MethodParameter} with the type
	 * for the current level set to the specified value.
	 * @param typeIndex the new type index
	 * @since 5.2
	 */
	public MethodParameter withTypeIndex(int typeIndex) {
		return nested(this.nestingLevel, typeIndex);
	}

	/**
	 * Set the type index for the current nesting level.
	 * @param typeIndex the corresponding type index
	 * (or {@code null} for the default type index)
	 * @see #getNestingLevel()
	 * @deprecated since 5.2 in favor of {@link #withTypeIndex}
	 */
	@Deprecated
	public void setTypeIndexForCurrentLevel(int typeIndex) {
		getTypeIndexesPerLevel().put(this.nestingLevel, typeIndex);
	}

	/**
	 * Return the type index for the current nesting level.
	 * @return the corresponding type index, or {@code null}
	 * if none specified (indicating the default type index)
	 * @see #getNestingLevel()
	 */
	@Nullable
	public Integer getTypeIndexForCurrentLevel() {
		return getTypeIndexForLevel(this.nestingLevel);
	}

	/**
	 * Return the type index for the specified nesting level.
	 * @param nestingLevel the nesting level to check
	 * @return the corresponding type index, or {@code null}
	 * if none specified (indicating the default type index)
	 */
	@Nullable
	public Integer getTypeIndexForLevel(int nestingLevel) {
		return getTypeIndexesPerLevel().get(nestingLevel);
	}

	/**
	 * Obtain the (lazily constructed) type-indexes-per-level Map.
	 */
	private Map<Integer, Integer> getTypeIndexesPerLevel() {
		if (this.typeIndexesPerLevel == null) {
			this.typeIndexesPerLevel = new HashMap<>(4);
		}
		//可以这么理解：List<String> -> (1,0) 对应list (2,0) 对应String
		return this.typeIndexesPerLevel;
	}

	/**
	 * Return a variant of this {@code MethodParameter} which points to the
	 * same parameter but one nesting level deeper.
	 * @since 4.3
	 */
	public MethodParameter nested() {
		return nested(null);
	}

	/**
	 * Return a variant of this {@code MethodParameter} which points to the
	 * same parameter but one nesting level deeper.
	 * @param typeIndex the type index for the new nesting level
	 * @since 5.2
	 *
	 * 用于处理嵌套泛型场景：嵌套泛型在 Java 中是一种复杂的类型结构，它涉及到多层泛型的嵌套使用。
	 * 例如，List<Map<String, Integer>> 就是一个典型的嵌套泛型结构。在这种情况下，MethodParameter.nested 方法能够帮助我们获取嵌套泛型的参数信息。
	 * 使用场景
	 * 	嵌套泛型解析：在处理复杂的泛型结构时，可以使用 nested 方法来获取每一层的参数信息。
	 * 	动态代理与反射：在动态代理和反射操作中，当涉及到嵌套泛型的处理时，nested 方法可以用来获取嵌套类型的参数信息。
	 * 优点和优势
	 * 	灵活性：nested 方法提供了灵活的方式来处理嵌套泛型结构，能够适应各种复杂的类型嵌套场景。
	 * 	精确性：它能够精确地定位到嵌套泛型中的特定参数类型，这对于类型检查和转换非常重要。
	 * 	与 Spring 框架的集成：在 Spring 框架中，MethodParameter 类广泛用于处理方法参数的元数据，
	 * 	nested 方法在处理嵌套泛型时与其他 Spring 框架功能紧密结合，提供了一致的开发体验。
	 *
	 * MethodParameter methodParam = new MethodParameter(getClass().getMethod("exampleMethod", List.class), 0);
	 * MethodParameter nestedParam = methodParam.nested(1);
	 * Type genericParameterType = nestedParam.getGenericParameterType();
	 * System.out.println(genericParameterType); // 输出嵌套泛型的类型信息
	 */
	public MethodParameter nested(@Nullable Integer typeIndex) {
		MethodParameter nestedParam = this.nestedMethodParameter;
		if (nestedParam != null && typeIndex == null) {
			return nestedParam;
		}
		//这个方法复制了一份原先的数据，同时维护了对应的 nestingLevel -> typeIndex 的索引，方便直接使用
		nestedParam = nested(this.nestingLevel + 1, typeIndex);
		if (typeIndex == null) {
			this.nestedMethodParameter = nestedParam;
		}
		return nestedParam;
	}

	private MethodParameter nested(int nestingLevel, @Nullable Integer typeIndex) {
		MethodParameter copy = clone();
		copy.nestingLevel = nestingLevel;
		if (this.typeIndexesPerLevel != null) {
			copy.typeIndexesPerLevel = new HashMap<>(this.typeIndexesPerLevel);
		}
		if (typeIndex != null) {
			copy.getTypeIndexesPerLevel().put(copy.nestingLevel, typeIndex);
		}
		copy.parameterType = null;
		copy.genericParameterType = null;
		return copy;
	}

	/**
	 * Return whether this method indicates a parameter which is not required:
	 * either in the form of Java 8's {@link java.util.Optional}, any variant
	 * of a parameter-level {@code Nullable} annotation (such as from JSR-305
	 * or the FindBugs set of annotations), or a language-level nullable type
	 * declaration or {@code Continuation} parameter in Kotlin.
	 * @since 4.3
	 */
	public boolean isOptional() {
		// 这里判断了 @Nullable 的情况
		return (getParameterType() == Optional.class || hasNullableAnnotation() ||
				(KotlinDetector.isKotlinReflectPresent() &&
						KotlinDetector.isKotlinType(getContainingClass()) &&
						KotlinDelegate.isOptional(this)));
	}

	/**
	 * Check whether this method parameter is annotated with any variant of a
	 * {@code Nullable} annotation, e.g. {@code javax.annotation.Nullable} or
	 * {@code edu.umd.cs.findbugs.annotations.Nullable}.
	 */
	private boolean hasNullableAnnotation() {
		for (Annotation ann : getParameterAnnotations()) {
			if ("Nullable".equals(ann.annotationType().getSimpleName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return a variant of this {@code MethodParameter} which points to
	 * the same parameter but one nesting level deeper in case of a
	 * {@link java.util.Optional} declaration.
	 * @since 4.3
	 * @see #isOptional()
	 * @see #nested()
	 */
	public MethodParameter nestedIfOptional() {
		return (getParameterType() == Optional.class ? nested() : this);
	}

	/**
	 * Return a variant of this {@code MethodParameter} which refers to the
	 * given containing class.
	 * @param containingClass a specific containing class (potentially a
	 * subclass of the declaring class, e.g. substituting a type variable)
	 * @since 5.2
	 * @see #getParameterType()
	 */
	public MethodParameter withContainingClass(@Nullable Class<?> containingClass) {
		MethodParameter result = clone();
		result.containingClass = containingClass;
		result.parameterType = null;
		return result;
	}

	/**
	 * Set a containing class to resolve the parameter type against.
	 */
	@Deprecated
	void setContainingClass(Class<?> containingClass) {
		this.containingClass = containingClass;
		this.parameterType = null;
	}

	/**
	 * Return the containing class for this method parameter.
	 * @return a specific containing class (potentially a subclass of the
	 * declaring class), or otherwise simply the declaring class itself
	 * @see #getDeclaringClass()
	 */
	public Class<?> getContainingClass() {
		Class<?> containingClass = this.containingClass;
		return (containingClass != null ? containingClass : getDeclaringClass());
	}

	/**
	 * Set a resolved (generic) parameter type.
	 */
	@Deprecated
	void setParameterType(@Nullable Class<?> parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * Return the type of the method/constructor parameter.
	 * @return the parameter type (never {@code null})
	 */
	public Class<?> getParameterType() {
		Class<?> paramType = this.parameterType;
		if (paramType != null) {
			return paramType;
		}
		/**
		 * Java 中嵌套类的两种类型：静态嵌套类（Static Nested Class） 和 内部类（Inner Class）
		 * getDeclaringClass()：返回声明该方法的直接类（即定义该方法的类）。
		 * getContainingClass()：返回包含该方法的实际类（即从运行时角度真正“拥有”该方法的类）
		 * 在大多数情况下，两者是相同的:
		 * class MyClass {
		 *     void myMethod() {}
		 * }
		 *
		 * MyClass obj = new MyClass();
		 * Method method = obj.getClass().getMethod("myMethod");
		 * System.out.println(method.getDeclaringClass() == method.getContainingClass()); // true
		 *
		 * 两者的区别场景：嵌套类
		 * 静态嵌套类（Static Nested Class）：静态嵌套类是作为外部类的静态成员定义的类。它的方法属于它自己，而不是外部类：
		 * class Outer {
		 *     static class Nested {
		 *         void nestedMethod() {}
		 *     }
		 * }
		 *
		 * Outer.Nested obj = new Outer.Nested();
		 * Method method = obj.getClass().getMethod("nestedMethod");
		 * System.out.println(method.getDeclaringClass() == method.getContainingClass()); // true
		 * 在这个例子中，nestedMethod 的 getDeclaringClass() 和 getContainingClass() 都是 Outer.Nested.class，所以条件不成立。
		 *
		 * 内部类（Inner Class）：内部类（非静态嵌套类）是定义在外部类内部的类，并且可以访问外部类的实例变量和方法：
		 * class Outer {
		 *     class Inner {
		 *         void innerMethod() {}
		 *     }
		 * }
		 *
		 * Outer outer = new Outer();
		 * Outer.Inner inner = outer.new Inner();
		 * Method method = inner.getClass().getMethod("innerMethod");
		 * method.getDeclaringClass() → Outer.Inner.class（声明方法的类）
		 * method.getContainingClass() → Outer.Inner.class（包含方法的类）
		 * 两者仍然相同，因为 Inner 类的方法是直接定义在 Inner 类中的。
		 *
		 * 内部类的方法覆盖外部类的方法：
		 * class Outer {
		 *     void outerMethod() {}
		 *
		 *     class Inner extends Outer {
		 *         @Override
		 *         void outerMethod() {}
		 *     }
		 * }
		 *
		 * Outer outer = new Outer();
		 * Outer.Inner inner = outer.new Inner();
		 * Method method = inner.getClass().getMethod("outerMethod");
		 * method.getDeclaringClass() → Outer.Inner.class（方法是在 Inner 类中声明的）
		 * method.getContainingClass() → Outer.Inner.class（方法属于 Inner 类）
		 * 两者仍然相同
		 *
		 * 触发条件的场景：匿名内部类：匿名内部类是内部类的一种特殊形式，它没有类名并且通常在创建对象时定义。当匿名内部类覆盖了外部类的方法时，可能会触发这个条件：
		 * class Outer {
		 *     void outerMethod() {}
		 * }
		 *
		 * Outer outer = new Outer() { // 匿名内部类
		 *     @Override
		 *     void outerMethod() {}
		 * };
		 *
		 * Method method = outer.getClass().getMethod("outerMethod");
		 * method.getDeclaringClass() → 匿名内部类的 Class（例如：Outer$1.class）
		 * method.getContainingClass() → Outer.class（因为匿名内部类的方法实际上是包含在外部类中的）
		 * 所以条件 getContainingClass() != getDeclaringClass() 成立！
		 *
		 * getContainingClass() != getDeclaringClass() 主要出现在 匿名内部类覆盖外部类方法 的场景中。
		 * 匿名内部类的方法在字节码层面实际上是定义在外部类中的，所以 getContainingClass() 返回外部类，而 getDeclaringClass() 返回匿名内部类本身。
		 * 这种情况在普通嵌套类（静态或非静态）中较少见，更多出现在动态生成的类（如匿名类或 Lambda 表达式）中。
		 * 这个条件主要用于框架内部的反射逻辑，帮助区分方法的实际归属，特别是在复杂的类加载和继承场景中。
		 */
		if (getContainingClass() != getDeclaringClass()) {
			paramType = ResolvableType.forMethodParameter(this, null, 1).resolve();
		}
		if (paramType == null) {
			paramType = computeParameterType();
		}
		this.parameterType = paramType;
		return paramType;
	}

	/**
	 * Return the generic type of the method/constructor parameter.
	 *
	 * 泛型参数解析：方法解析并返回方法或构造函数参数的泛型类型。
	 * 内部类构造器处理：特殊处理内部类构造器的参数索引，因为内部类构造器会有一个额外的参数用于表示外部类实例。
	 * 缓存机制：结果被缓存以便后续调用时不需要重复解析。
	 *
	 * public void exampleMethod(List<String> param1, Map<Integer, Double> param2) {}
	 * 当你调用 getGenericParameterType() 并传入参数索引 0 时，应该返回 List<String>.class；传入参数索引 1 时，应该返回 Map<Integer, Double>.class。
	 *
	 * @return the parameter type (never {@code null})
	 * @since 3.0
	 */
	public Type getGenericParameterType() {
		Type paramType = this.genericParameterType;
		// 缓存检查： 首先检查 this.genericParameterType 是否已经赋值。如果是，则直接返回缓存的值。
		if (paramType == null) {
			/**
			 * 处理返回值类型（parameterIndex < 0）：
			 * 	 如果 parameterIndex 小于 0，表示要获取的是方法的返回值类型而不是参数类型。
			 * 	 获取方法对象，并根据是否是 Kotlin 类型来决定如何获取泛型返回类型。
			 */
			if (this.parameterIndex < 0) {
				Method method = getMethod();
				paramType = (method != null ?
						(KotlinDetector.isKotlinReflectPresent() && KotlinDetector.isKotlinType(getContainingClass()) ?
						KotlinDelegate.getGenericReturnType(method) : method.getGenericReturnType()) : void.class);
			}
			else {
				/**
				 * 处理参数类型（parameterIndex >= 0）：
				 * 	如果 parameterIndex 大于等于 0，表示要获取的是方法或构造函数的参数类型。
				 * 	获取可执行对象（方法或构造函数）的泛型参数类型数组。
				 * 	如果可执行对象是构造函数，并且它属于一个内部类，则调整参数索引以考虑内部类构造器的特殊参数（外部类实例）。
				 * 	根据索引从泛型参数类型数组中获取具体的类型，如果索引超出范围，则调用 computeParameterType() 方法计算参数类型。
				 */
				Type[] genericParameterTypes = this.executable.getGenericParameterTypes();
				int index = this.parameterIndex;
				if (this.executable instanceof Constructor &&
						ClassUtils.isInnerClass(this.executable.getDeclaringClass()) &&
						genericParameterTypes.length == this.executable.getParameterCount() - 1) {
					// Bug in javac: type array excludes enclosing instance parameter
					// for inner classes with at least one generic constructor parameter,
					// so access it with the actual parameter index lowered by 1
					// javac中的错误：对于至少有一个泛型构造函数参数的内部类，类型数组排除了封闭实例参数，因此，在访问时，实际参数的索引应减1
					index = this.parameterIndex - 1;
				}
				paramType = (index >= 0 && index < genericParameterTypes.length ?
						genericParameterTypes[index] : computeParameterType());
			}
			// 缓存结果：将解析得到的 paramType 赋值给 this.genericParameterType 以便后续复用。
			this.genericParameterType = paramType;
		}
		return paramType;
	}

	private Class<?> computeParameterType() {
		if (this.parameterIndex < 0) {
			Method method = getMethod();
			if (method == null) {
				return void.class;
			}
			if (KotlinDetector.isKotlinReflectPresent() && KotlinDetector.isKotlinType(getContainingClass())) {
				return KotlinDelegate.getReturnType(method);
			}
			return method.getReturnType();
		}
		return this.executable.getParameterTypes()[this.parameterIndex];
	}

	/**
	 * Return the nested type of the method/constructor parameter.
	 *
	 * public void exampleMethod(Map<String, List<Integer>> param) {}
	 * 用 getNestedParameterType() 方法解析 param 的类型：
	 *  1.nestingLevel 为 2，表示嵌套了两层泛型。
	 *  2.getGenericParameterType() 返回 Map<String, List<Integer>>。
	 *  3.循环解析：
	 *   第一层：解析出 List<Integer>
	 *   第二层：解析出 Integer
	 *  4.返回 Integer.class
	 *
	 * @return the parameter type (never {@code null})
	 * @since 3.1
	 * @see #getNestingLevel()
	 */
	public Class<?> getNestedParameterType() {
		if (this.nestingLevel > 1) { // 表示当前参数类型是嵌套的泛型类型（如 Map<String, List<Integer>>）
			Type type = getGenericParameterType(); // 获取当前参数的泛型类型
			for (int i = 2; i <= this.nestingLevel; i++) {
				if (type instanceof ParameterizedType) {
					Type[] args = ((ParameterizedType) type).getActualTypeArguments();
					Integer index = getTypeIndexForLevel(i);
					type = args[index != null ? index : args.length - 1];
				}
				// TODO: Object.class if unresolvable
			}
			if (type instanceof Class) {
				return (Class<?>) type;
			}
			else if (type instanceof ParameterizedType) {
				Type arg = ((ParameterizedType) type).getRawType();
				if (arg instanceof Class) {
					return (Class<?>) arg;
				}
			}
			return Object.class;
		}
		else {
			return getParameterType();
		}
	}

	/**
	 * Return the nested generic type of the method/constructor parameter.
	 * @return the parameter type (never {@code null})
	 * @since 4.2
	 * @see #getNestingLevel()
	 */
	public Type getNestedGenericParameterType() {
		if (this.nestingLevel > 1) {
			Type type = getGenericParameterType();
			for (int i = 2; i <= this.nestingLevel; i++) {
				if (type instanceof ParameterizedType) {
					Type[] args = ((ParameterizedType) type).getActualTypeArguments();
					Integer index = getTypeIndexForLevel(i);
					type = args[index != null ? index : args.length - 1];
				}
			}
			return type;
		}
		else {
			return getGenericParameterType();
		}
	}

	/**
	 * Return the annotations associated with the target method/constructor itself.
	 */
	public Annotation[] getMethodAnnotations() {
		return adaptAnnotationArray(getAnnotatedElement().getAnnotations());
	}

	/**
	 * Return the method/constructor annotation of the given type, if available.
	 * @param annotationType the annotation type to look for
	 * @return the annotation object, or {@code null} if not found
	 */
	@Nullable
	public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
		A annotation = getAnnotatedElement().getAnnotation(annotationType);
		return (annotation != null ? adaptAnnotation(annotation) : null);
	}

	/**
	 * Return whether the method/constructor is annotated with the given type.
	 * @param annotationType the annotation type to look for
	 * @since 4.3
	 * @see #getMethodAnnotation(Class)
	 */
	public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType) {
		return getAnnotatedElement().isAnnotationPresent(annotationType);
	}

	/**
	 * Return the annotations associated with the specific method/constructor parameter.
	 */
	public Annotation[] getParameterAnnotations() {
		Annotation[] paramAnns = this.parameterAnnotations;
		if (paramAnns == null) {
			/**
			 * public void exampleMethod(
			 *     @RequestParam String name,
			 *     @Valid @RequestBody User user
			 * ) {}
			 * Annotation[][] annotationsArray = method.getParameterAnnotations();
			 * annotationsArray[0]：包含 name 参数上的注解，即 [@RequestParam]
			 * annotationsArray[1]：包含 user 参数上的注解，即 [@Valid, @RequestBody]
			 */
			Annotation[][] annotationArray = this.executable.getParameterAnnotations();
			int index = this.parameterIndex;
			if (this.executable instanceof Constructor &&
					ClassUtils.isInnerClass(this.executable.getDeclaringClass()) &&
					annotationArray.length == this.executable.getParameterCount() - 1) {
				// Bug in javac in JDK <9: annotation array excludes enclosing instance parameter
				// for inner classes, so access it with the actual parameter index lowered by 1
				/**
				 * 在 Java 中，内部类的构造器会隐式地包含一个对外部类实例的引用作为第一个参数。
				 * 例如，如果你有一个外部类 Outer 和一个内部类 Inner，编译器会为 Inner 的构造器生成一个额外的参数，用于传递 Outer 的实例。
				 * 这个参数是编译器自动生成的，用户在代码中通常看不到它。
				 * class Outer {
				 *     class Inner {
				 *         Inner() {
				 *             // 编译器会自动生成一个参数接收外部类实例
				 *         }
				 *     }
				 * }
				 * 在 JDK 9 之前的版本中，getParameterAnnotations() 方法在处理内部类的构造器时，会返回一个注解数组，但这个数组的长度会比实际的参数数量少 1。
				 * 这是因为编译器生成的注解数组没有包含那个隐式的外部类实例参数的注解信息。
				 * 具体来说，对于一个内部类的构造器：
				 * 如果构造器有 n 个显式参数（用户在代码中定义的参数），编译器会生成一个带有 n + 1 个参数的构造器（多出的那个参数用于接收外部类实例）。
				 * 但是，getParameterAnnotations() 方法返回的注解数组长度却是 n，缺少了对那个隐式参数的注解信息。
				 *
				 * 注解数组的索引范围是 [0, annotationArray.length - 1]，而用户的代码参数从索引 1 开始（隐式参数占用了索引 0）。
				 * 如果不调整，用户的参数索引会比注解数组的实际索引 大 1。通过 parameterIndex - 1，可以将用户的逻辑索引映射到注解数组的实际索引上。
				 */
				index = this.parameterIndex - 1;
			}
			paramAnns = (index >= 0 && index < annotationArray.length ?
					adaptAnnotationArray(annotationArray[index]) : EMPTY_ANNOTATION_ARRAY);
			this.parameterAnnotations = paramAnns;
		}
		return paramAnns;
	}

	/**
	 * Return {@code true} if the parameter has at least one annotation,
	 * {@code false} if it has none.
	 * @see #getParameterAnnotations()
	 */
	public boolean hasParameterAnnotations() {
		return (getParameterAnnotations().length != 0);
	}

	/**
	 * Return the parameter annotation of the given type, if available.
	 * @param annotationType the annotation type to look for
	 * @return the annotation object, or {@code null} if not found
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <A extends Annotation> A getParameterAnnotation(Class<A> annotationType) {
		Annotation[] anns = getParameterAnnotations();
		for (Annotation ann : anns) {
			if (annotationType.isInstance(ann)) {
				return (A) ann;
			}
		}
		return null;
	}

	/**
	 * Return whether the parameter is declared with the given annotation type.
	 * @param annotationType the annotation type to look for
	 * @see #getParameterAnnotation(Class)
	 */
	public <A extends Annotation> boolean hasParameterAnnotation(Class<A> annotationType) {
		return (getParameterAnnotation(annotationType) != null);
	}

	/**
	 * Initialize parameter name discovery for this method parameter.
	 * <p>This method does not actually try to retrieve the parameter name at
	 * this point; it just allows discovery to happen when the application calls
	 * {@link #getParameterName()} (if ever).
	 */
	public void initParameterNameDiscovery(@Nullable ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	/**
	 * Return the name of the method/constructor parameter.
	 * @return the parameter name (may be {@code null} if no
	 * parameter name metadata is contained in the class file or no
	 * {@link #initParameterNameDiscovery ParameterNameDiscoverer}
	 * has been set to begin with)
	 */
	@Nullable
	public String getParameterName() {
		if (this.parameterIndex < 0) {
			return null;
		}
		ParameterNameDiscoverer discoverer = this.parameterNameDiscoverer;
		if (discoverer != null) {
			String[] parameterNames = null;
			if (this.executable instanceof Method) {
				parameterNames = discoverer.getParameterNames((Method) this.executable);
			}
			else if (this.executable instanceof Constructor) {
				parameterNames = discoverer.getParameterNames((Constructor<?>) this.executable);
			}
			if (parameterNames != null) {
				this.parameterName = parameterNames[this.parameterIndex];
			}
			this.parameterNameDiscoverer = null;
		}
		return this.parameterName;
	}


	/**
	 * A template method to post-process a given annotation instance before
	 * returning it to the caller.
	 * <p>The default implementation simply returns the given annotation as-is.
	 * @param annotation the annotation about to be returned
	 * @return the post-processed annotation (or simply the original one)
	 * @since 4.2
	 */
	protected <A extends Annotation> A adaptAnnotation(A annotation) {
		return annotation;
	}

	/**
	 * A template method to post-process a given annotation array before
	 * returning it to the caller.
	 * <p>The default implementation simply returns the given annotation array as-is.
	 * @param annotations the annotation array about to be returned
	 * @return the post-processed annotation array (or simply the original one)
	 * @since 4.2
	 */
	protected Annotation[] adaptAnnotationArray(Annotation[] annotations) {
		return annotations;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MethodParameter)) {
			return false;
		}
		MethodParameter otherParam = (MethodParameter) other;
		return (getContainingClass() == otherParam.getContainingClass() &&
				ObjectUtils.nullSafeEquals(this.typeIndexesPerLevel, otherParam.typeIndexesPerLevel) &&
				this.nestingLevel == otherParam.nestingLevel &&
				this.parameterIndex == otherParam.parameterIndex &&
				this.executable.equals(otherParam.executable));
	}

	@Override
	public int hashCode() {
		return (31 * this.executable.hashCode() + this.parameterIndex);
	}

	@Override
	public String toString() {
		Method method = getMethod();
		return (method != null ? "method '" + method.getName() + "'" : "constructor") +
				" parameter " + this.parameterIndex;
	}

	@Override
	public MethodParameter clone() {
		return new MethodParameter(this);
	}

	/**
	 * Create a new MethodParameter for the given method or constructor.
	 * <p>This is a convenience factory method for scenarios where a
	 * Method or Constructor reference is treated in a generic fashion.
	 * @param methodOrConstructor the Method or Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 * @return the corresponding MethodParameter instance
	 * @deprecated as of 5.0, in favor of {@link #forExecutable}
	 */
	@Deprecated
	public static MethodParameter forMethodOrConstructor(Object methodOrConstructor, int parameterIndex) {
		if (!(methodOrConstructor instanceof Executable)) {
			throw new IllegalArgumentException(
					"Given object [" + methodOrConstructor + "] is neither a Method nor a Constructor");
		}
		return forExecutable((Executable) methodOrConstructor, parameterIndex);
	}

	/**
	 * Create a new MethodParameter for the given method or constructor.
	 * <p>This is a convenience factory method for scenarios where a
	 * Method or Constructor reference is treated in a generic fashion.
	 * @param executable the Method or Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 * @return the corresponding MethodParameter instance
	 * @since 5.0
	 */
	public static MethodParameter forExecutable(Executable executable, int parameterIndex) {
		if (executable instanceof Method) {
			return new MethodParameter((Method) executable, parameterIndex);
		}
		else if (executable instanceof Constructor) {
			return new MethodParameter((Constructor<?>) executable, parameterIndex);
		}
		else {
			throw new IllegalArgumentException("Not a Method/Constructor: " + executable);
		}
	}

	/**
	 * Create a new MethodParameter for the given parameter descriptor.
	 * <p>This is a convenience factory method for scenarios where a
	 * Java 8 {@link Parameter} descriptor is already available.
	 * @param parameter the parameter descriptor
	 * @return the corresponding MethodParameter instance
	 * @since 5.0
	 */
	public static MethodParameter forParameter(Parameter parameter) {
		return forExecutable(parameter.getDeclaringExecutable(), findParameterIndex(parameter));
	}

	protected static int findParameterIndex(Parameter parameter) {
		Executable executable = parameter.getDeclaringExecutable();
		Parameter[] allParams = executable.getParameters();
		// Try first with identity checks for greater performance.
		// 使用身份检查快速查找：
		//	遍历所有参数，尝试通过身份检查（parameter == allParams[i]）快速找到目标参数。这种方式性能较高，因为直接比较对象引用。
		for (int i = 0; i < allParams.length; i++) {
			if (parameter == allParams[i]) {
				return i;
			}
		}
		// Potentially try again with object equality checks in order to avoid race
		// conditions while invoking java.lang.reflect.Executable.getParameters().
		// 使用对象相等性检查再次查找：
		//		如果身份检查未找到匹配参数，则再次遍历所有参数，这次使用对象相等性检查（parameter.equals(allParams[i])）
		//		这一步是为了处理可能的竞态条件，确保参数查找的准确性。
		for (int i = 0; i < allParams.length; i++) {
			if (parameter.equals(allParams[i])) {
				return i;
			}
		}
		/**
		 * ### 为什么进行两次查找
		 *
		 * `findParameterIndex` 方法内部进行两次查找的原因是为了兼顾性能和准确性。
		 *
		 * ### 第一次查找（身份检查）
		 * - **方式**：通过 `parameter == allParams[i]` 进行身份检查。
		 * - **目的**：快速定位参数。如果参数对象与参数数组中的某个元素是同一个实例（即引用相同），则直接返回该索引。
		 * - **优势**：身份检查的速度非常快，因为它直接比较对象的内存地址。
		 * 			  在大多数情况下，参数对象和参数数组中的元素是同一个实例，因此这种方法可以快速找到结果。
		 *
		 * ### 第二次查找（对象相等性检查）
		 * - **方式**：通过 `parameter.equals(allParams[i])` 进行对象相等性检查。
		 * - **目的**：确保准确性。如果身份检查未能找到匹配项，可能是由于参数数组是通过反射动态获取的，
		 * 			  导致参数对象与参数数组中的元素不是同一个实例，但它们在逻辑上是相等的。
		 * - **优势**：对象相等性检查可以处理参数对象与参数数组中的元素不是同一个实例，但内容相同的情况。
		 *            这确保了即使在反射操作导致参数对象重新创建的情况下，也能正确找到匹配的参数。
		 *
		 * ### 总结
		 *
		 * 两次查找结合了性能和准确性的考虑。第一次查找利用身份检查快速定位参数，适用于大多数情况；
		 * 如果第一次查找失败，第二次查找通过对象相等性检查确保结果的准确性，避免了反射操作可能引发的问题。
		 * 这种设计确保了方法在大多数情况下高效运行，同时在特殊情况也能保证正确的结果。
		 */
		throw new IllegalArgumentException("Given parameter [" + parameter +
				"] does not match any parameter in the declaring executable");
	}

	private static int validateIndex(Executable executable, int parameterIndex) {
		int count = executable.getParameterCount();
		Assert.isTrue(parameterIndex >= -1 && parameterIndex < count,
				() -> "Parameter index needs to be between -1 and " + (count - 1));
		return parameterIndex;
	}


	/**
	 * Inner class to avoid a hard dependency on Kotlin at runtime.
	 */
	private static class KotlinDelegate {

		/**
		 * Check whether the specified {@link MethodParameter} represents a nullable Kotlin type,
		 * an optional parameter (with a default value in the Kotlin declaration) or a
		 * {@code Continuation} parameter used in suspending functions.
		 */
		public static boolean isOptional(MethodParameter param) {
			Method method = param.getMethod();
			int index = param.getParameterIndex();
			if (method != null && index == -1) {
				KFunction<?> function = ReflectJvmMapping.getKotlinFunction(method);
				return (function != null && function.getReturnType().isMarkedNullable());
			}
			KFunction<?> function;
			Predicate<KParameter> predicate;
			if (method != null) {
				if (param.getParameterType().getName().equals("kotlin.coroutines.Continuation")) {
					return true;
				}
				function = ReflectJvmMapping.getKotlinFunction(method);
				predicate = p -> KParameter.Kind.VALUE.equals(p.getKind());
			}
			else {
				Constructor<?> ctor = param.getConstructor();
				Assert.state(ctor != null, "Neither method nor constructor found");
				function = ReflectJvmMapping.getKotlinFunction(ctor);
				predicate = p -> (KParameter.Kind.VALUE.equals(p.getKind()) ||
						KParameter.Kind.INSTANCE.equals(p.getKind()));
			}
			if (function != null) {
				int i = 0;
				for (KParameter kParameter : function.getParameters()) {
					if (predicate.test(kParameter)) {
						if (index == i++) {
							return (kParameter.getType().isMarkedNullable() || kParameter.isOptional());
						}
					}
				}
			}
			return false;
		}

		/**
		 * Return the generic return type of the method, with support of suspending
		 * functions via Kotlin reflection.
		 */
		static private Type getGenericReturnType(Method method) {
			try {
				KFunction<?> function = ReflectJvmMapping.getKotlinFunction(method);
				if (function != null && function.isSuspend()) {
					return ReflectJvmMapping.getJavaType(function.getReturnType());
				}
			}
			catch (UnsupportedOperationException ex) {
				// probably a synthetic class - let's use java reflection instead
			}
			return method.getGenericReturnType();
		}

		/**
		 * Return the return type of the method, with support of suspending
		 * functions via Kotlin reflection.
		 */
		static private Class<?> getReturnType(Method method) {
			try {
				KFunction<?> function = ReflectJvmMapping.getKotlinFunction(method);
				if (function != null && function.isSuspend()) {
					Type paramType = ReflectJvmMapping.getJavaType(function.getReturnType());
					if (paramType == Unit.class) {
						paramType = void.class;
					}
					return ResolvableType.forType(paramType).resolve(method.getReturnType());
				}
			}
			catch (UnsupportedOperationException ex) {
				// probably a synthetic class - let's use java reflection instead
			}
			return method.getReturnType();
		}
	}

}
