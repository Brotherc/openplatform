package com.brotherc.documentcenter.model.dto.developer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "开发者删除入参")
public class DeveloperDeleteDTO {

    @NotNull(message = "开发者ID不能为空")
    @Schema(description = "开发者ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long developerId;

}
