package com.brotherc.documentcenter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeveloperAuthenticateStatusEnum {

    AUTHENTICATED_SUCCESS(1, "已认证"),
    UNAUTHORIZED(2, "未认证"),
    PROCESS(3, "审核中"),
    AUTHENTICATED_FAIL(4, "认证未通过");

    private final int code;
    private final String name;

}
