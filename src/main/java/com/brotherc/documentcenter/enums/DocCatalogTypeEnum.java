package com.brotherc.documentcenter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocCatalogTypeEnum {

    CATALOG(1, "目录"),
    DOC(2, "文章");

    private final int code;
    private final String name;

}
