package com.brotherc.documentcenter.model.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigInteger;

@Data
@Schema(description = "菜单创建入参")
public class MenuAddDTO {

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50")
    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "菜单描述")
    @Size(max = 255, message = "菜单描述长度不能超过255")
    private String description;

    @Schema(description = "父菜单id")
    private Long parentId = 0L;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态，1：禁用，2：启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @NotBlank(message = "访问路径不能为空")
    @Size(max = 255, message = "访问路径长度不能超过255")
    @Schema(description = "访问路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String path;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    @Max(value = 99999, message = "排序不能大于99999")
    @Min(value = 0, message = "排序不能小于0")
    private BigInteger sort;

    @Schema(description = "文档分组id")
    private Long docCatalogGroupId;

}
