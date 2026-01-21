package com.bj;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.springframework.context.ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME;

/**
 * @Author: xingbinjie
 * @Desc:
 * @Version: 0.0.1
 * @Date: 2026/1/9
 */
public class Entry {

	public static void main(String[] args) {
		DefaultListableBeanFactory defaultListableBeanFactory = new DefaultListableBeanFactory();
		ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) defaultListableBeanFactory.getBean(ENVIRONMENT_BEAN_NAME);

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.refresh();
		configurableEnvironment = (ConfigurableEnvironment) applicationContext.getBean(ENVIRONMENT_BEAN_NAME);
	}

}