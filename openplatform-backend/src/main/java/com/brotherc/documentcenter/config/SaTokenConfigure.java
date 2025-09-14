package com.brotherc.documentcenter.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * [Sa-Token 权限认证] 全局配置类
 */
@Configuration
public class SaTokenConfigure {

    private static final String[] excludePath = new String[]{
            "/druid/**",
            "/webjars/**",
            "/favicon.ico",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/actuator/**",
            "/doc.html",
            "/swagger-ui/index.html",
            "/*/portal/**"
    };

    /**
     * 注册 [Sa-Token全局过滤器]
     */
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 指定 [拦截路由]
                .addInclude("/**")
                // 指定 [放行路由]
                .addExclude(excludePath)
                // 指定[认证函数]: 每次请求执行
                .setAuth(obj -> SaRouter
                        .match("/**")
                        .notMatch("/user/login")
                        .notMatch(SaHttpMethod.OPTIONS)
                        .check(r -> StpUtil.checkLogin()))
                .setError(e -> {
                    // 设置响应头
                    SaHolder.getResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
                    // 创建响应DTO
                    if (e instanceof SaTokenException saTokenException) {
                        return ResponseDTO.fail(1100000 + saTokenException.getCode(), saTokenException.getMessage(), null);
                    } else {
                        return ResponseDTO.fail(ExceptionEnum.SYS_ERROR.getCode(), e.getMessage(), null);
                    }
                })
                ;
    }

    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

}