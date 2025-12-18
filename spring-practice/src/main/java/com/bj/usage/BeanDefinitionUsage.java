package com.bj.usage;

import com.bj.entity.User;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @Author: xingbinjie
 * @Desc:
 * @Version: 0.0.1
 * @Date: 2025/12/17
 */
public class BeanDefinitionUsage {
	/**
	 * 在Spring中，我们可以如何去定义一个Bean?
	 * 1. <bean/>
	 * 2. @Bean
	 * 3. @Component(@Service,@Controller)
	 * 4. 通过BeanDefinition
	 */
	public static void main(String[] args) {
		// 定义了一个BeanDefinition
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		// 当前Bean对象的类型
		beanDefinition.setBeanClass(User.class);
		// 将BeanDefinition注册到BeanFactory中
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition("user", beanDefinition);

		beanDefinition.setScope("prototype"); // 设置作用域
		beanDefinition.setInitMethodName("init"); // 设置初始化方法
		beanDefinition.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE); // 设置自动装配模型

		// 获取Bean
		System.out.println(beanFactory.getBean("user"));
	}

}
