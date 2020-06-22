# 一、Spring Boot 入门

## 1、启动器

```
 <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
```

SpringbootWEB启动器，帮助我们自动引入web常用依赖。

可通过以下网址查找所需启动器配置在maven中即可：

​				https://docs.spring.io/spring-boot/docs/2.4.0-SNAPSHOT/reference/html/using-spring-boot.html#using-boot-starter

## 2、启动类，程序入口类

```java
/**
 * @SpringBootApplication 主配置类标注
 */
@SpringBootApplication
public class DemoApplication {


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```

@SpringBootApplication: SpringBoot核心注解类，标注着该类为Springboot主配置类，是一个组合注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {

```

@SpringBootConfiguration：Spring Boot配置注解，标识这是Spring Boot的一个配置类，内部油@Configuration标注，配置类也是一个组件，由Component标注

@EnableAutoConfiguration：Spring Boot自动配置注解，帮助我们完成自动配置，只有标记该注解才会开启自动配置功能。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {

```

@AutoConfigurationPackage：自动配置包，查看源码可知，其上由@Import(AutoConfigurationPackages.Registrar.class)标注

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AutoConfigurationPackages.Registrar.class)
public @interface AutoConfigurationPackage {

```

@Import:是Spring底层注解，其作用是给容器中导入某个组件类，@Import(AutoConfigurationPackages.Registrar.class)是将Registrar这个组件类导入到容器中。

查看源码可知，导入组件类的具体实现为方法

```java
	/**
	 * {@link ImportBeanDefinitionRegistrar} to store the base package from the importing
	 * configuration.
	 */
	static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
			register(registry, new PackageImports(metadata).getPackageNames().toArray(new String[0]));
		}
```

通过对 new PackageImports(metadata).getPackageNames().toArray(new String[0])进行检索（idea使用alt+f8），发现他的值为主程序类所在的包名，所以@AutoConfigurationPackage注解的作用就是**将主配置类所在包及下面的所有子包下的所有组件扫描到spring容器中**  

@Import(AutoConfigurationImportSelector.class):给容器中导入AutoConfigurationImportSelector组件，AutoConfigurationImportSelector是导入哪些组件的选择器，将符合条件的@Configuration导入到当前SpringBoot创建并使用的IOC容器中，需要导入的组件将以全类名的方式返回，通过查看源码可知，以下方法为导入方法：

```
protected AutoConfigurationEntry getAutoConfigurationEntry(AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return EMPTY_ENTRY;
		}
		AnnotationAttributes attributes = getAttributes(annotationMetadata);
		//导入自动配置类
		List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
		configurations = removeDuplicates(configurations);
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);
		checkExcludedClasses(configurations, exclusions);
		configurations.removeAll(exclusions);
		configurations = getConfigurationClassFilter().filter(configurations);
		fireAutoConfigurationImportEvents(configurations, exclusions);
		return new AutoConfigurationEntry(configurations, exclusions);
	}
```

通过debug运行，可以发现注释行代码会返回很多的自动配置类(xxxAutoConfiguration)

<img src="D:\gitproject\study-code\study-demo-for-springboot\img\AutoConfigurationImportSelector.png" style="zoom:100%;" />

查看 getCandidateConfiguration()方法

```
protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		//让SpringFactoryLoader加载一些组件名称
		List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
				getBeanClassLoader());
		Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
				+ "are using a custom packaging, make sure that file is correct.");
		return configurations;
	}
```

查看SpringFactoriesLoader.loadFactoryNames（）方法

```
public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
		String factoryTypeName = factoryType.getName();
		return loadSpringFactories(classLoader).getOrDefault(factoryTypeName, Collections.emptyList());
	}
```

查看loadSpringFactories（）方法

```
private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
		MultiValueMap<String, String> result = cache.get(classLoader);
		if (result != null) {
			return result;
		}

		try {
		//如果类加载器不为空，加载类路径下的META-INF/spring.factories文件
			Enumeration<URL> urls = (classLoader != null ?
					classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
					ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
			result = new LinkedMultiValueMap<>();
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				UrlResource resource = new UrlResource(url);
				Properties properties = PropertiesLoaderUtils.loadProperties(resource);
				for (Map.Entry<?, ?> entry : properties.entrySet()) {
					String factoryTypeName = ((String) entry.getKey()).trim();
					for (String factoryImplementationName : StringUtils.commaDelimitedListToStringArray((String) entry.getValue())) {
						result.add(factoryTypeName, factoryImplementationName.trim());
					}
				}
			}
			cache.put(classLoader, result);
			return result;
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load factories from location [" +
					FACTORIES_RESOURCE_LOCATION + "]", ex);
		}
	}
```

**Spring Boot在启动的时候从类路径下的META-INF/spring.factories中获取EnableAutoConfiguration指定的值，将这些值作为自动配置类导入到容器中，自动配置类就生效，帮我们进行自动配置工作**  

![](D:\gitproject\study-code\study-demo-for-springboot\img\META-INF-spring-factories.png)