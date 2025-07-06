package com.brotherc.documentcenter.model.entity;

import com.brotherc.documentcenter.model.entity.common.BaseEntity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Table(name = "doc_catalog_api")
public class DocCatalogApi extends BaseEntity {

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long docCatalogApiId;

    /**
     * 分组id
     */
    private Long docCatalogGroupId;

    /**
     * 文章目录id
     */
    private Long docCatalogId;

    /**
     * 父目录id
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * api分类id
     */
    private Long apiInfoCategoryId;

}
