package com.brotherc.documentcenter.model.dto.apiinfocategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "api分类批量删除入参")
public class ApiInfoCategoryBatchDeleteDTO {

    @NotEmpty(message = "api分类id列表不能为空")
    @Schema(description = "api分类id列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> apiInfoCategoryIdList;

}
