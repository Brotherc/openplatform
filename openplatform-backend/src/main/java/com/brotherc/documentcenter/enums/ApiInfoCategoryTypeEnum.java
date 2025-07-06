package com.brotherc.documentcenter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiInfoCategoryTypeEnum {

    CATEGORY(1, "分类"),
    API(2, "api");

    private final int code;
    private final String name;

}
