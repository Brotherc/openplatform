package com.brotherc.documentcenter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublishStatusEnum {

    UN_PUBLISH(1, "未发布"),
    PUBLISH(2, "已发布");

    private final int code;
    private final String name;

}
