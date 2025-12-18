package com.bj.usage;

import com.bj.entity.User;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Arrays;

/**
 * @Author: xingbinjie
 * @Desc:
 * @Version: 0.0.1
 * @Date: 2025/12/17
 */
public class BeanFactoryUsage {

	/**
	 * Spring中比较核心的是BeanFactory的实现类是DefaultListableBeanFactory
	 * 实现了很多接口，表示，它拥有很多功能：
	 * 	1. AliasRegistry：支持别名功能，一个名字可以对应多个别名
	 * 	2. BeanDefinitionRegistry：可以注册、保存、移除、获取某个BeanDefinition
	 * 	3. BeanFactory：Bean工厂，可以根据某个bean的名字、或类型、或别名获取某个Bean对象
	 * 	4. SingletonBeanRegistry：可以直接注册、获取某个单例Bean
	 * 	5. SimpleAliasRegistry：它是一个类，实现了AliasRegistry接口中所定义的功能，支持别名功能
	 * 	6. ListableBeanFactory：在BeanFactory的基础上，增加了其他功能，可以获取所有BeanDefinition的beanNames，
	 * 		可以根据某个类型获取对应的beanNames，可以根据某个类型获取{类型：对应的Bean}的映射关系
	 * 	7. HierarchicalBeanFactory：在BeanFactory的基础上，添加了获取父BeanFactory的功能
	 * 	8. DefaultSingletonBeanRegistry：它是一个类，实现了SingletonBeanRegistry接口，拥有了直接注册、获取某个单例Bean的功能
	 * 	9. ConfigurableBeanFactory：在HierarchicalBeanFactory和SingletonBeanRegistry的基础上，
	 * 		添加了设置父BeanFactory、类加载器（表示可以指定某个类加载器进行类的加载）、
	 * 		设置Spring EL表达式解析器（表示该BeanFactory可以解析EL表达式）、
	 * 		设置类型转化服务（表示该BeanFactory可以进行类型转化）、
	 * 		可以添加BeanPostProcessor（表示该BeanFactory支持Bean的后置处理器），
	 * 		可以合并BeanDefinition，可以销毁某个Bean等等功能
	 * 10. FactoryBeanRegistrySupport：支持了FactoryBean的功能
	 * 11. AutowireCapableBeanFactory：是直接继承了BeanFactory，在BeanFactory的基础上，支持在创建Bean的过程中能对Bean进行自动装配
	 * 12. AbstractBeanFactory：实现了ConfigurableBeanFactory接口，继承了FactoryBeanRegistrySupport，
	 * 		这个BeanFactory的功能已经很全面了，但是不能自动装配和获取beanNames
	 * 13. ConfigurableListableBeanFactory：继承了ListableBeanFactory、AutowireCapableBeanFactory、ConfigurableBeanFactory
	 * 14. AbstractAutowireCapableBeanFactory：继承了AbstractBeanFactory，实现了AutowireCapableBeanFactory，拥有了自动装配的功能
	 * 15. DefaultListableBeanFactory：继承了AbstractAutowireCapableBeanFactory，
	 * 	实现了ConfigurableListableBeanFactory接口和BeanDefinitionRegistry接口，所以DefaultListableBeanFactory的功能很强大
	 */
	public static void DefaultListableBeanFactory() {
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
		beanDefinition.setBeanClass(User.class);

		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		// 注册BeanDefinition
		beanFactory.registerBeanDefinition("user", beanDefinition);
		// 注册别名
		beanFactory.registerAlias("user", "user1");
		// 注册BeanPostProcessor
		beanFactory.addBeanPostProcessor(new BeanPostProcessor(){});
		// 获取Bean对象
		System.out.println(beanFactory.getBean("user1"));
		// 根据类型获取beanNames
		System.out.println(Arrays.toString(beanFactory.getBeanNamesForType(User.class)));
	}

}
