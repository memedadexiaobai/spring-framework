package com.bj.usage;

import com.bj.entity.User;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.*;

/**
 * @Author: xingbinjie
 * @Desc:
 * 	ApplicationContext和BeanFactory架构图：
 * 		processon链接：https://www.processon.com/view/link/5f7c271be0b34d0711f4b116
 * 		密码：ggmc
 * @Version: 0.0.1
 * @Date: 2025/12/17
 */
public class ApplicationContextUsage {

	/**
	 * ApplicationContext是个接口，可以把它理解为一个特殊的BeanFactory
	 * 1. HierarchicalBeanFactory：拥有获取父BeanFactory的功能
	 * 2. ListableBeanFactory：拥有获取beanNames的功能
	 * 3. ResourcePatternResolver：资源加载器，可以一次性获取多个资源（文件资源等等）
	 * 4. EnvironmentCapable：可以获取运行时环境（没有设置运行时环境功能）
	 * 5. ApplicationEventPublisher：拥有广播事件的功能（没有添加事件监听器的功能）
	 * 6. MessageSource：拥有国际化功能
	 */
	public static void ApplicationContext() {
	}

	/**
	 * 1. ConfigurableApplicationContext：继承了ApplicationContext接口，
	 * 	 增加了：添加事件监听器、添加BeanFactoryPostProcessor、设置Environment，获取ConfigurableListableBeanFactory等功能
	 * 2. AbstractApplicationContext：实现了ConfigurableApplicationContext接口
	 * 3. GenericApplicationContext：继承了AbstractApplicationContext，实现了BeanDefinitionRegistry接口，
	 * 	 拥有了所有ApplicationContext的功能，并且可以注册BeanDefinition，注意这个类中有一个属性(DefaultListableBeanFactory beanFactory)
	 * 4. AnnotationConfigRegistry：可以单独注册某个为类为BeanDefinition（可以处理该类上的@Configuration注解，已经可以处理@Bean注解），同时可以扫描
	 * 5. AnnotationConfigApplicationContext：继承了GenericApplicationContext，实现了AnnotationConfigRegistry接口，拥有了以上所有的功能
	 */
	public static void AnnotationConfigApplicationContext() throws IOException {
		/**
		 * 国际化
		 *   先定义一个MessageSource: {@link #messageSource()}
		 *   有了这个Bean，你可以在你任意想要进行国际化的地方使用该MessageSource。同时，因为ApplicationContext也拥有国家化的功能，所以可以直接这么用：
		 */
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
		annotationConfigApplicationContext.getMessage("test", null, new Locale("en_CN"));

		/**
		 * 资源加载：ApplicationContext还拥有资源加载的功能，比如，可以直接利用ApplicationContext获取某个文件
		 * 这个功能用到了策略模式
 		 */
		Resource resource = annotationConfigApplicationContext.getResource("file://D:\\IdeaProjects\\spring-framework\\spring-practice\\src\\main\\java\\com\\bj\\entity\\User.java");
		System.out.println(resource.contentLength());

		resource = annotationConfigApplicationContext.getResource("classpath:com/bj/entity/User.class");
		System.out.println(resource.contentLength());

		Resource[] resources = annotationConfigApplicationContext.getResources("classpath:com/bj/service/*.class");
		for (Resource resource1 : resources) {
			System.out.println(resource1.contentLength());
		}

		/**
		 * 获取运行时环境
		 *  可以利用 @PropertySource("classpath:application.properties") 来使得某个properties文件中的参数添加到运行时环境中
		 */
		// 获取JVM所允许的操作系统的环境
		annotationConfigApplicationContext.getEnvironment().getSystemEnvironment();
		// 获取JVM本身的一些属性，包括-D所设置的
		annotationConfigApplicationContext.getEnvironment().getSystemProperties();
		// 还可以直接获取某个环境或properties文件中的属性
		annotationConfigApplicationContext.getEnvironment().getProperty("bj");

		/**
		 * 事件发布
		 * 	先定义一个事件监听器：{@link #applicationListener()}
		 * 	然后发布一个事件
		 */
		annotationConfigApplicationContext.publishEvent("bj");

		/**
		 * 类型转化
		 * 	1.PropertyEditor：JDK中提供的类型转化工具类
		 * 	  向Spring中注册PropertyEditor：{@link #customEditorConfigurer()}
		 * 	  假设现在有如下Bean：{@link UserService }，那么test属性就能正常的完成属性赋值
		 * 	2.ConversionService：Spring中提供的类型转化服务，它比PropertyEditor更强大
		 * 	  定义一个Converter：{@link StringToUserConverter }
		 * 	  向Spring中注册ConversionService：{@link #conversionService()}
		 * 	3.TypeConverter：整合了PropertyEditor和ConversionService的功能，是Spring内部用的
		 */
		StringToUserPropertyEditor propertyEditor = new StringToUserPropertyEditor();
		propertyEditor.setAsText("1");
		User value = (User) propertyEditor.getValue();
		System.out.println(value);

		DefaultConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new StringToUserConverter());
		value = conversionService.convert("1", User.class);
		System.out.println(value);

		SimpleTypeConverter typeConverter = new SimpleTypeConverter();
		typeConverter.registerCustomEditor(User.class, new StringToUserPropertyEditor());
		//typeConverter.setConversionService(conversionService);
		value = typeConverter.convertIfNecessary("1", User.class);
		System.out.println(value);

		/**
		 * BeanPostProcessor：Bean的后置处理器，可以在创建每个Bean的过程中进行干涉，是属于BeanFactory中一个属性
		 * BeanFactoryPostProcessor：Bean工厂的后置处理器，是属于ApplicationContext中的一个属性，
		 * 	是ApplicationContext在实例化一个BeanFactory后，可以利用BeanFactoryPostProcessor继续处理BeanFactory
		 *  可以通过BeanFactoryPostProcessor间接的设置BeanFactory，比如上文中的CustomEditorConfigurer就是一个BeanFactoryPostProcessor，
		 *  我们可以通过它向BeanFactory中添加自定义的PropertyEditor
		 * FactoryBean：允许自定义一个对象通过FactoryBean间接的放到Spring容器中成为一个Bean
		 * 	和@Bean的区别是：因为@Bean也可以自定义一个对象，让这个对象成为一个Bean
		 * 	  区别在于利用FactoryBean可以更加强大，因为你通过定义一个XxFactoryBean的类，可以再去实现Spring中的其他接口，
		 * 	  比如如果你实现了BeanFactoryAware接口，那么你可以在你的XxFactoryBean中获取到Bean工厂，从而使用Bean工厂做更多你想做的，而@Bean则不行
		 */
	}

	/**
	 * 继承了AbstractApplicationContext，但是相对于AnnotationConfigApplicationContext而言，功能没有AnnotationConfigApplicationContext强大，比如不能注册BeanDefinition
	 */
	public static void ClassPathXmlApplicationContext() {
	}

	/**
	 * 国际化
	 */
	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("messages");
		return messageSource;
	}

	@Bean
	public ApplicationListener applicationListener() {
		return event -> System.out.println("接收到了一个事件");
	}

	static class StringToUserPropertyEditor extends PropertyEditorSupport implements PropertyEditor {

		@Override
		public void setAsText(String text) throws IllegalArgumentException {
			User user = new User();
			user.setName(text);
			this.setValue(user);
		}

	}

	static class StringToUserConverter implements ConditionalGenericConverter {

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			return sourceType.getType().equals(String.class) && targetType.getType().equals(User.class);
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(new ConvertiblePair(String.class, User.class));
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			User user = new User();
			user.setName((String)source);
			return user;
		}
	}

	@Bean
	public CustomEditorConfigurer customEditorConfigurer() {
		CustomEditorConfigurer customEditorConfigurer = new CustomEditorConfigurer();
		Map<Class<?>, Class<? extends PropertyEditor>> propertyEditorMap = new HashMap<>();
		propertyEditorMap.put(User.class, StringToUserPropertyEditor.class);
		customEditorConfigurer.setCustomEditors(propertyEditorMap);
		return customEditorConfigurer;
	}

	@Bean
	public ConversionServiceFactoryBean conversionService() {
		ConversionServiceFactoryBean conversionServiceFactoryBean = new ConversionServiceFactoryBean();
		conversionServiceFactoryBean.setConverters(Collections.singleton(new StringToUserConverter()));
		return conversionServiceFactoryBean;
	}

	@Component
	public class UserService {

		@Value("true")
		User test;

		public void test() {
			System.out.println(test);
		}
	}


}