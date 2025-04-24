package com.brotherc.documentcenter.model.dto.doccataloggroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "分组状态更新入参")
public class DocCatalogGroupStatusUpdateDTO {

    @NotNull(message = "分组id不能为空")
    @Schema(description = "分组id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long docCatalogGroupId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态，1：未发布，2：已发布")
    private Integer status;

}
