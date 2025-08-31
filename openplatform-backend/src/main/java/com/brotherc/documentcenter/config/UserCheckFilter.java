package com.brotherc.documentcenter.config;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.stp.StpUtil;
import com.brotherc.documentcenter.dao.UserRepository;
import com.brotherc.documentcenter.enums.UserStatusEnum;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(-99)
public class UserCheckFilter implements WebFilter {

    private final List<PathPattern> excludePatterns;

    // 需要排除的路径模式
    private static final String[] excludePath = new String[]{
            "/user/login",
            "/druid/**",
            "/webjars/**",
            "/favicon.ico",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/actuator/**",
            "/doc.html",
            "/swagger-ui/index.html"
    };

    private final UserRepository userRepository;

    public UserCheckFilter(UserRepository userRepository) {
        this.userRepository = userRepository;

        PathPatternParser parser = new PathPatternParser();
        this.excludePatterns =
                java.util.Arrays.stream(excludePath)
                        .map(parser::parse)
                        .toList();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        PathContainer path = exchange.getRequest().getPath().pathWithinApplication();

        // 如果匹配排除路径，则直接放行
        for (PathPattern pattern : excludePatterns) {
            if (pattern.matches(path)) {
                return chain.filter(exchange);
            }
        }

        return Mono.deferContextual(context -> {
            long loginId;
            try {
                SaReactorSyncHolder.setContext(exchange);
                loginId = Long.parseLong(StpUtil.getLoginId().toString());
            } finally {
                SaReactorSyncHolder.clearContext();
            }

            return userRepository.findByUserIdAndIsDel(loginId, 0)
                    .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.USER_LOGIN_REPEAT)))
                    .flatMap(user -> {
                        if (user.getStatus() == UserStatusEnum.DISABLED.getCode()) {
                            return Mono.error(new BusinessException(ExceptionEnum.USER_STATUS_ERROR));
                        }
                        return chain.filter(exchange);
                    }).doOnError(ex -> {
                        try {
                            SaReactorSyncHolder.setContext(exchange);
                            StpUtil.logout();
                        } finally {
                            SaReactorSyncHolder.clearContext();
                        }
                    });
        });
    }

}
