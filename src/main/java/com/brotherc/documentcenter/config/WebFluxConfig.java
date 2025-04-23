package com.brotherc.documentcenter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        // 注册 ReactivePageableHandlerMethodArgumentResolver
        configurer.addCustomResolver(new ReactivePageableHandlerMethodArgumentResolver());
    }

}