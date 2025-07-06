package com.brotherc.documentcenter.model.dto.doccatalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文档目录状态更新入参")
public class DocCatalogStatusUpdateDTO {

    @NotNull(message = "文档目录id不能为空")
    @Schema(description = "文档目录id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long docCatalogId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态，1：未发布，2：已发布", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

}
