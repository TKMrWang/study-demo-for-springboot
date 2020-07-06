package com.study.demo.config;

import com.study.demo.Interceptor.MyInterceptor1;
import com.study.demo.Interceptor.MyInterceptor2;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MyInterceptor1()) // 添加拦截器
                .addPathPatterns("/**") // 添加拦截路径
                .excludePathPatterns(// 添加排除拦截路径
                        "/hello").order(0);//执行顺序

        registry.addInterceptor(new MyInterceptor2()) // 添加拦截器
                .addPathPatterns("/**") // 添加拦截路径
                .excludePathPatterns(// 添加排除拦截路径
                        "/hello").order(1);//执行顺序

        super.addInterceptors(registry);
    }
}
