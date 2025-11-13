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
		ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) applicationContext.getBean(ENVIRONMENT_BEAN_NAME);
	}

}
