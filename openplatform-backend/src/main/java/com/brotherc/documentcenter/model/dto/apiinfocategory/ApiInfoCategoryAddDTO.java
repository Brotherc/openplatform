package com.brotherc.documentcenter.model.dto.apiinfocategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "api分类创建入参")
public class ApiInfoCategoryAddDTO {

    @NotBlank(message = "名称不能为空")
    @Size(max = 50, message = "名称长度不能超过50")
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "父分类id")
    private Long parentId = 0L;

    @NotNull(message = "类型不能为空")
    @Schema(description = "类型，1：分类，2：api", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

}
