package com.brotherc.documentcenter.model.dto.doccataloggroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "分组创建入参")
public class DocCatalogGroupAddDTO {

    @NotBlank(message = "分组名称不能为空")
    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "描述")
    private String description;

}
