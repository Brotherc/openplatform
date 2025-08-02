package com.brotherc.documentcenter.model.dto.apiinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "api信息导入入参")
public class ApiInfoImportDTO {

    @NotBlank(message = "url不能为空")
    @Schema(description = "url", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;

}
