package com.brotherc.documentcenter.model.dto.doccataloggroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查询分组入参")
public class DocCatalogGroupQueryDTO {

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "状态，1：未发布，2：已发布")
    private Integer status;

}
