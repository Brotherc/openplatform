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
@Table("menu")
public class Menu extends BaseEntity {

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 父菜单id
     */
    private Long parentId;

    /**
     * 状态，1：禁用，2：启用
     */
    private Integer status;

    /**
     * 访问路径
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 分组id
     */
    private Long docCatalogGroupId;

}

