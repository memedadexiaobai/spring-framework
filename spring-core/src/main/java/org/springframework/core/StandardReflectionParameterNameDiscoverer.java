/*
 * Copyright 2002-2017 the original author or authors.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.springframework.lang.Nullable;

/**
 * {@link ParameterNameDiscoverer} implementation which uses JDK 8's reflection facilities
 * for introspecting parameter names (based on the "-parameters" compiler flag).
 *
 * @author Juergen Hoeller
 * @since 4.0
 * @see java.lang.reflect.Method#getParameters()
 * @see java.lang.reflect.Parameter#getName()
 */
public class StandardReflectionParameterNameDiscoverer implements ParameterNameDiscoverer {

	@Override
	@Nullable
	public String[] getParameterNames(Method method) {
		return getParameterNames(method.getParameters());
	}

	@Override
	@Nullable
	public String[] getParameterNames(Constructor<?> ctor) {
		return getParameterNames(ctor.getParameters());
	}

	@Nullable
	private String[] getParameterNames(Parameter[] parameters) {
		String[] parameterNames = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Parameter param = parameters[i];
			/**
			 * param.isNamePresent() 是 Java 8 开始加在 java.lang.reflect.Parameter 上的方法，用来快速判断“当前编译单元里是否保留了形参名”。
			 * 	返回 true → 可以通过 param.getName() 拿到源码里的真实变量名（如 userId）。
			 * 	返回 false → 只能拿到 arg0、arg1 这种占位符，不要指望用变量名去做映射。
			 *
			 * 底层原理
			 * 	依赖 -parameters 编译开关（JDK 8+ 新增）。
			 * 	加 -parameters：class 文件里会多一个 MethodParameters 属性，存真实名。
			 * 	不加：class 文件只有类型，没有名字，反射只能拿到 arg0、arg1。
			 */
			if (!param.isNamePresent()) {
				return null;
			}
			parameterNames[i] = param.getName();
		}
		return parameterNames;
	}

}
