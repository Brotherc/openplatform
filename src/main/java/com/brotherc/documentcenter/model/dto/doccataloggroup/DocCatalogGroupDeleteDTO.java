package com.brotherc.documentcenter.model.dto.doccataloggroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "分组删除入参")
public class DocCatalogGroupDeleteDTO {

    @NotNull(message = "分组id不能为空")
    @Schema(description = "分组id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long docCatalogGroupId;

}
