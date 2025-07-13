package com.brotherc.documentcenter.model.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "菜单删除入参")
public class MenuDeleteDTO {

    @NotNull(message = "菜单id不能为空")
    @Schema(description = "菜单id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long menuId;

}
