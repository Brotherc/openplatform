package com.brotherc.documentcenter.model.dto.doccatalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigInteger;

@Data
@Schema(description = "文档目录创建入参")
public class DocCatalogAddDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过50")
    @Schema(description = "目录名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "分组id不能为空")
    @Schema(description = "分组id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long docCatalogGroupId;

    @Schema(description = "父目录id")
    private Long parentId = 0L;

    @NotNull(message = "类型不能为空")
    @Schema(description = "类型，1：目录，2：文章", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    @Max(value = 99999, message = "排序不能大于99999")
    @Min(value = 0, message = "排序不能小于0")
    private BigInteger sort;

}
