package com.bj.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.springframework.context.ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME;

@Configuration
public class ApplicationContextConfig {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
//		applicationContext.addBeanFactoryPostProcessor();
		applicationContext.refresh();
		/**
		 * 实例化之前：
		 * 	InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation
		 * 实例化过程中：
		 * 	有一个步骤是通过SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors 来推断构造方法
		 * 实例后：
		 * 	MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition
		 * 	如果允许暴露早期索引的话：
		 * 	 	创建一个ObjectFactory，放到singletonFactories，实际实现：调用SmartInstantiationAwareBeanPostProcessor#getEarlyBeanReference 获取一个早期的索引
		 * 	 填充属性：
		 * 	 	InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation
		 * 	 	InstantiationAwareBeanPostProcessor#postProcessProperties
		 * 	 	InstantiationAwareBeanPostProcessor#postProcessPropertyValues
		 * 	 初始前：
		 * 	 	BeanPostProcessor#postProcessBeforeInitialization 先执行 PostConstruct 标记方法，再执行 InitializingBean#afterPropertiesSet，最后调用其他自定义初始化方法
		 * 	 初始化：
		 * 	 	InitializingBean#afterPropertiesSet或者回调初始化方法
		 * 	 初始化后：
		 * 	 	BeanPostProcessor#postProcessAfterInitialization
		 * 	 销毁：
		 * 	 	实现了DisposableBean或者AutoCloseable接口
		 * 	 	DestructionAwareBeanPostProcessor#requiresDestruction判断是否需要销毁操作
		 * 	 	有close方法、shutdown方法
		 * 	 销毁执行：
		 * 	 	DestructionAwareBeanPostProcessor#postProcessBeforeDestruction
		 * 	 	DisposableBean#destroy
		 * 	 	调用自定义的销毁方法
		 */
		ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) applicationContext.getBean(ENVIRONMENT_BEAN_NAME);
	}

}
