package com.brotherc.documentcenter.model.dto.doccatalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文档目录api查询入参")
public class DocCatalogApiQueryDTO {

    @NotNull(message = "文档目录id不能为空")
    @Schema(description = "文档目录id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long docCatalogId;

}
