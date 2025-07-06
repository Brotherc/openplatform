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
@Table(name = "api_info_category")
public class ApiInfoCategory extends BaseEntity {

    /**
     * api分类id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(value = "api_info_category_id")
    private Long apiInfoCategoryId;

    /**
     * api分类名称
     */
    @Column(value = "name")
    private String name;

    /**
     * 父分类id
     */
    @Column(value = "parent_id")
    private Long parentId;

    /**
     * 类型，1：分类，2：api
     */
    @Column(value = "type")
    private Integer type;

    /**
     * 状态，1：未发布，2：已发布
     */
    @Column(value = "status")
    private Integer status;

}
