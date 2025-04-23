package com.brotherc.documentcenter.common.log;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class LoggingWebFilter implements WebFilter {

    public static final List<MediaType> legalLogMediaTypes = Arrays.asList(
            MediaType.TEXT_XML,
            MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_JSON_UTF8,
            MediaType.TEXT_PLAIN,
            MediaType.TEXT_XML);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        final String method = request.getMethod().name();
        final String path = request.getURI().getPath();
        final String query = request.getURI().getQuery();
        final String headers = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("""
                                
                HttpMethod : {}
                Uri        : {}
                Headers    : {}""", method, path + (StringUtils.isNotBlank(query) ? "?" + query : ""), headers);
        MediaType contentType = request.getHeaders().getContentType();
        if (HttpMethod.POST.name().equals(method)) {
            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        String bodyString = new String(bytes, StandardCharsets.UTF_8);

                        if (legalLogMediaTypes.contains(contentType)) {
                            // 打印请求参数
                            log.info("""

                                    Body       :
                                    {}""", bodyString);
                        }

                        DataBufferUtils.release(dataBuffer);
                        Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                            DataBuffer buffer = exchange.getResponse().bufferFactory()
                                    .wrap(bytes);
                            return Mono.just(buffer);
                        });

                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(
                                exchange.getRequest()) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return cachedFlux;
                            }
                        };
                        return chain.filter(
                                exchange.mutate().request(mutatedRequest).response(new LoggingResponseDecorator(exchange.getResponse())).build()
                        );
                    });
        }
        return chain.filter(exchange.mutate().response(new LoggingResponseDecorator(exchange.getResponse())).build());
    }

}