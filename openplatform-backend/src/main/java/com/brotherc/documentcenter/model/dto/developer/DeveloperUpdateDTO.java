package com.brotherc.documentcenter.model.dto.developer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "开发者更新入参")
public class DeveloperUpdateDTO {

    @NotNull(message = "开发者ID不能为空")
    @Schema(description = "开发者ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long developerId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态，1：禁用，2：启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @NotNull(message = "开发者类型不能为空")
    @Schema(description = "开发者类型，1：个人，2：企业", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer developerType;

}
