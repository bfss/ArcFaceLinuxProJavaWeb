package com.bfss.arcfacelinuxprojavaweb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer{

    @Value("${app.upload-dir}")
    private String uploadDir;

    // 由于采用前后端分离设计，所以需要配置CORS避免跨域问题
    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("*")
        .allowedHeaders("*");
    }

    // 添加静态资源映射，实现前端访问图片
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry
        .addResourceHandler("images/**")
        .addResourceLocations("file:" + uploadDir);
    }
}
