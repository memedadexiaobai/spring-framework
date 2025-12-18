package com.bj.usage;

import com.bj.entity.User;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

/**
 * @Author: xingbinjie
 * @Desc:
 * @Version: 0.0.1
 * @Date: 2025/12/17
 */
public class BeanDefinitionReaderUsage {

	static DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

	/**
	 * AnnotatedBeanDefinitionReader：可以直接把某个类转换为BeanDefinition，并且会解析该类上的注解
	 *  注意：它能解析的注解是：@Conditional，@Scope、@Lazy、@Primary、@DependsOn、@Role、@Description
	 */
	public static void AnnotatedBeanDefinitionReader() {
		AnnotatedBeanDefinitionReader annotatedBeanDefinitionReader = new AnnotatedBeanDefinitionReader(beanFactory);
		// 将User.class解析为BeanDefinition
		annotatedBeanDefinitionReader.register(User.class);

		System.out.println(beanFactory.getBean("user"));
	}

	/**
	 * XmlBeanDefinitionReader：可以解析<bean/>标签
	 */
	public static void XmlBeanDefinitionReader() {
		XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
		int i = xmlBeanDefinitionReader.loadBeanDefinitions("spring.xml");

		System.out.println(beanFactory.getBean("user"));
	}

	/**
	 * 这个并不是BeanDefinitionReader，但是它的作用和BeanDefinitionReader类似，它可以进行扫描，扫描某个包路径，对扫描到的类进行解析
	 * 	比如，扫描到的类上如果存在@Component注解，那么就会把这个类解析为一个BeanDefinition
	 */
	public static void ClassPathBeanDefinitionScanner() {
		ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(beanFactory);
		classPathBeanDefinitionScanner.scan("com.bj");
	}

}
