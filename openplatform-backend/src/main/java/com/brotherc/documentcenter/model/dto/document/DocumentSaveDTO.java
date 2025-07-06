package com.brotherc.documentcenter.model.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文章保存入参")
public class DocumentSaveDTO {

    @NotNull(message = "文档目录id不能为空")
    @Schema(description = "文档目录id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long docCatalogId;

    @Schema(description = "文章内容")
    private String content;

}
