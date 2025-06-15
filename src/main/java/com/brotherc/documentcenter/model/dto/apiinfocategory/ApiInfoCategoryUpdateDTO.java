package com.brotherc.documentcenter.model.dto.apiinfocategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "api分类更新入参")
public class ApiInfoCategoryUpdateDTO {

    @NotNull(message = "api分类id不能为空")
    @Schema(description = "api分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long apiInfoCategoryId;

    @NotBlank(message = "名称不能为空")
    @Size(max = 50, message = "名称长度不能超过50")
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "父分类id")
    private Long parentId = 0L;

}
