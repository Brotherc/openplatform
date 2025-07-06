package com.brotherc.documentcenter.model.dto.apiinfocategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "api分类状态更新入参")
public class ApiInfoCategoryStatusUpdateDTO {

    @NotNull(message = "api分类id不能为空")
    @Schema(description = "api分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long apiInfoCategoryId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态，1：未发布，2：已发布", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

}
