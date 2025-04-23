package com.brotherc.documentcenter.exception;

import lombok.Getter;

public class BusinessException extends RuntimeException {

    @Getter
    private final Integer code;

    @Getter
    private final String msg;

    public BusinessException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMsg());
        this.code = exceptionEnum.getCode();
        this.msg = exceptionEnum.getMsg();
    }

}
