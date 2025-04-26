package com.brotherc.documentcenter.model.dto.doccatalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigInteger;

@Data
@Schema(description = "文档目录更新入参")
public class DocCatalogUpdateDTO {

    @NotNull(message = "文档目录id不能为空")
    @Schema(description = "文档目录id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long docCatalogId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过50")
    @Schema(description = "目录名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "父目录id")
    private Long parentId = 0L;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    @Max(value = 99999, message = "排序不能大于99999")
    @Min(value = 0, message = "排序不能小于0")
    private BigInteger sort;

}
