package com.brotherc.documentcenter.model.dto.doccataloggroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigInteger;

@Data
@Schema(description = "分组创建入参")
public class DocCatalogGroupAddDTO {

    @NotBlank(message = "分组名称不能为空")
    @Size(max = 50, message = "分组名称长度不能超过50")
    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "分组描述")
    @Size(max = 255, message = "分组描述长度不能超过255")
    private String description;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    @Max(value = 99999, message = "排序不能大于99999")
    @Min(value = 0, message = "排序不能小于0")
    private BigInteger sort;

}
