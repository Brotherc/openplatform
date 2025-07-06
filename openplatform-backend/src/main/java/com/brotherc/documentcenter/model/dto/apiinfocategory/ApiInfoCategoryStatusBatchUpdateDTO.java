package com.brotherc.documentcenter.model.dto.apiinfocategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "api分类状态批量更新入参")
public class ApiInfoCategoryStatusBatchUpdateDTO {

    @NotEmpty(message = "api分类id列表不能为空")
    @Schema(description = "api分类id列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> apiInfoCategoryIdList;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态，1：未发布，2：已发布", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

}
