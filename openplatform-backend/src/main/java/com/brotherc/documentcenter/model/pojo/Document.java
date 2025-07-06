package com.brotherc.documentcenter.model.pojo;

import com.brotherc.documentcenter.model.entity.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Document extends BaseEntity {

    /**
     * 文章目录id
     */
    private Long id;

    /**
     * 文章目录名称
     */
    private String name;

    /**
     * 分组id
     */
    private Long docCatalogGroupId;

    /**
     * 父目录id
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 文章内容
     */
    private String content;

}
