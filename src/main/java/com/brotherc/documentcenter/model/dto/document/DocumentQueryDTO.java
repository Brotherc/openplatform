package com.brotherc.documentcenter.model.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文章详情查询入参")
public class DocumentQueryDTO {

    @NotNull(message = "文章id不能为空")
    @Schema(description = "文章id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

}
