package com.brotherc.documentcenter.model.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查询菜单入参")
public class MenuQueryDTO {

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "状态，1：禁用，2：启用")
    private Integer status;

}
