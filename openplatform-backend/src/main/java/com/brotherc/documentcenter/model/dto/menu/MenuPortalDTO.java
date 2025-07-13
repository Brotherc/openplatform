package com.brotherc.documentcenter.model.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "门户菜单详情")
public class MenuPortalDTO {

    @Schema(description = "标识")
    private String key;

    @Schema(description = "父标识")
    private String parentKey;

    @Schema(description = "名称")
    private String title;

    @Schema(description = "访问路径")
    private String path;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "子菜单")
    private List<MenuPortalDTO> children;

}
