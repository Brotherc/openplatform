package com.brotherc.documentcenter.model.entity;

import com.brotherc.documentcenter.model.entity.common.BaseEntity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Table(name = "doc_catalog")
public class DocCatalog extends BaseEntity {

    /**
     * 文章目录id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(value = "doc_catalog_id")
    private Long docCatalogId;

    /**
     * 文章目录名称
     */
    @Column(value = "name")
    private String name;

    /**
     * 分组id
     */
    @Column(value = "doc_catalog_group_id")
    private Long docCatalogGroupId;

    /**
     * 父目录id
     */
    @Column(value = "parent_id")
    private Long parentId;

    /**
     * 类型，1：目录，2：文章
     */
    @Column(value = "type")
    private Integer type;

    /**
     * 状态，1：未发布，2：已发布
     */
    @Column(value = "status")
    private Integer status;

    /**
     * 排序
     */
    @Column(value = "sort")
    private Integer sort;

}
