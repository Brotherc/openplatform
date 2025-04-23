package com.brotherc.documentcenter.common.log;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
public class LoggingResponseDecorator extends ServerHttpResponseDecorator {

    public LoggingResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        DataBufferFactory bufferFactory = getDelegate().bufferFactory();

        return super.writeWith(Flux.from(body).buffer().map(dataBuffer -> {
            DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
            DataBuffer join = dataBufferFactory.join(dataBuffer);
            byte[] content = new byte[join.readableByteCount()];
            join.read(content);
            // 释放掉内存
            DataBufferUtils.release(join);
            String s = new String(content, StandardCharsets.UTF_8);

            List<String> strings = getDelegate().getHeaders().get(HttpHeaders.CONTENT_ENCODING);
            if (!CollectionUtils.isEmpty(strings) && strings.contains("gzip")) {
                GZIPInputStream gzipInputStream = null;
                try {
                    gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(content), content.length);
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(gzipInputStream, writer, "UTF-8");
                    s = writer.toString();

                } catch (IOException e) {
                    log.error("Gzip IO error", e);
                } finally {
                    if (gzipInputStream != null) {
                        try {
                            gzipInputStream.close();
                        } catch (IOException e) {
                            log.error("Gzip IO close error", e);
                        }
                    }
                }
            } else {
                s = new String(content, StandardCharsets.UTF_8);
            }

            // 打印请求响应值
            log.info("""

                    Response   :
                    {}""", s);
            return bufferFactory.wrap(content);
        }));
    }
}
