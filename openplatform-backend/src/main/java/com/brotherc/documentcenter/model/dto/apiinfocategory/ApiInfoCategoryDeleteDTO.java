package com.brotherc.documentcenter.model.dto.apiinfocategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "api分类删除入参")
public class ApiInfoCategoryDeleteDTO {

    @NotNull(message = "api分类id不能为空")
    @Schema(description = "api分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long apiInfoCategoryId;

}
