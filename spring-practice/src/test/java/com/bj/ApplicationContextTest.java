package com.bj;

import com.bj.config.ApplicationContextConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationContextTest {

	@Test
	void AnnotationConfigApplicationContext() {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.registerBean(ApplicationContextConfig.class);
		applicationContext.refresh();
	}

}