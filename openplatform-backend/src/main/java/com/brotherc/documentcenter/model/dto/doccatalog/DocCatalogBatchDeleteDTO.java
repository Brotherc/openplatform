package com.brotherc.documentcenter.model.dto.doccatalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "文档目录批量删除入参")
public class DocCatalogBatchDeleteDTO {

    @NotEmpty(message = "文档目录id列表不能为空")
    @Schema(description = "文档目录id列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> docCatalogIdList;

}
