package com.brotherc.documentcenter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    DISABLED(1, "禁用"),
    ENABLED(2, "启用");

    private final int code;
    private final String name;

}
