package com.brotherc.documentcenter.exception;

import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2)
public class GlobalWebExceptionHandler implements ErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        ResponseDTO<Void> responseDTO;
        if (ex instanceof BusinessException) {
            responseDTO = handleBusinessException((BusinessException) ex);
        } else if (ex instanceof WebExchangeBindException) {
            responseDTO = handleWebExchangeBindException((WebExchangeBindException) ex);
        } else if (ex instanceof ConstraintViolationException) {
            responseDTO = handleConstraintViolationException((ConstraintViolationException) ex);
        } else if (ex instanceof Exception) {
            responseDTO = handleException((Exception) ex);
        } else {
            responseDTO = ResponseDTO.fail(ExceptionEnum.SYS_ERROR.getCode(), ExceptionEnum.SYS_ERROR.getMsg(), null);
        }

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(responseDTO);
        } catch (JsonProcessingException e) {
            log.error("序列化异常", e);
            bytes = "".getBytes();
        }

        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer wrap = dataBufferFactory.wrap(bytes);

        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return exchange.getResponse().writeWith(Mono.just(wrap));
    }

    private ResponseDTO<Void> handleConstraintViolationException(ConstraintViolationException e) {

        StringBuilder errorMsg = new StringBuilder();
        for (ConstraintViolation violation : e.getConstraintViolations()) {
            errorMsg.append(violation.getPropertyPath()).append(":").append(violation.getMessage()).append(";");
        }
        String msg = errorMsg.substring(0, errorMsg.length() - 1);
        return ResponseDTO.fail(ExceptionEnum.SYS_CHECK_ERROR.getCode(), msg, null);
    }

    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            log.error(e.getMessage(), cause);
        } else {
            log.error("BusinessException: ", e);
        }

        return ResponseDTO.fail(e.getCode(), e.getMsg(), null);
    }

    private ResponseDTO<Void> handleWebExchangeBindException(WebExchangeBindException e) {
        log.error("BindException: ", e);

        StringBuilder errorMsg = new StringBuilder();
        for (FieldError error : e.getFieldErrors()) {
            errorMsg.append(error.getField()).append(":").append(error.getDefaultMessage()).append(";");
        }
        String msg = errorMsg.substring(0, errorMsg.length() - 1);

        return ResponseDTO.fail(ExceptionEnum.SYS_CHECK_ERROR.getCode(), msg, null);
    }

    @ExceptionHandler({Exception.class})
    public ResponseDTO<Void> handleException(Exception e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            log.error(e.getMessage(), cause);
        } else {
            log.error("", e);
        }

        return ResponseDTO.fail(ExceptionEnum.SYS_ERROR.getCode(), ExceptionEnum.SYS_ERROR.getMsg(), null);
    }

}
