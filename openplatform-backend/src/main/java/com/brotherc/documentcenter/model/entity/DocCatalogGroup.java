package com.brotherc.documentcenter.model.entity;

import com.brotherc.documentcenter.model.entity.common.BaseEntity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Table(name = "doc_catalog_group")
public class DocCatalogGroup extends BaseEntity {

    /**
     * 分组id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(value = "doc_catalog_group_id")
    private Long docCatalogGroupId;

    /**
     * 分组名称
     */
    @Column(value = "name")
    private String name;

    /**
     * 描述
     */
    @Column(value = "description")
    private String description;

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
