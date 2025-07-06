package com.brotherc.documentcenter.model.dto.apiinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "api信息详情查询入参")
public class ApiInfoQueryDTO {

    @NotNull(message = "api分类id不能为空")
    @Schema(description = "api分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long apiInfoCategoryId;

}
