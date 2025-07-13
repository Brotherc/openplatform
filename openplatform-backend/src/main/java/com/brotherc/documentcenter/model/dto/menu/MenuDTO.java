package com.brotherc.documentcenter.model.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "菜单详情")
public class MenuDTO {

    @Schema(description = "菜单id")
    private Long menuId;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "父菜单id")
    private Long parentId;

    @Schema(description = "状态，1：禁用，2：启用")
    private Integer status;

    @Schema(description = "访问路径")
    private String path;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "子菜单")
    private List<MenuDTO> children;

    @Schema(description = "创建用户")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新用户")
    private Long updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-否，1-是")
    private Integer isDel;

    @Schema(description = "文档分组id")
    private Long docCatalogGroupId;

}
