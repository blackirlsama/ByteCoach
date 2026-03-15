package com.shanyangcode.infintechatagent.config;

import com.shanyangcode.infintechatagent.interceptor.ObservabilityInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private ObservabilityInterceptor observabilityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(observabilityInterceptor).addPathPatterns("/**");
    }
}
