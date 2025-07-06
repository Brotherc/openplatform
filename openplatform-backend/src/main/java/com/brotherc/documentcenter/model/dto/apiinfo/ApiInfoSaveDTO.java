package com.brotherc.documentcenter.model.dto.apiinfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "api信息保存入参")
public class ApiInfoSaveDTO {

    @NotNull(message = "api分类id不能为空")
    @Schema(description = "api分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long apiInfoCategoryId;

    @NotBlank(message = "API名称不能为空")
    @Size(max = 150, message = "API名称长度不能超过150")
    @Schema(description = "API名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "中文名称不能为空")
    @Size(max = 50, message = "中文名称长度不能超过50")
    @Schema(description = "中文名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cnName;

    @Size(max = 200, message = "描述长度不能超过200")
    @Schema(description = "描述")
    private String description;

    @NotBlank(message = "请求方法不能为空")
    @Schema(description = "请求方法", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reqMethod;

    @Schema(description = "请求前缀")
    private String reqContextPath;

    @NotBlank(message = "请求路径不能为空")
    @Schema(description = "请求路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reqPath;

    @Schema(description = "请求参数")
    private ArrayNode queryParam;

    @Schema(description = "路径参数")
    private ArrayNode pathParam;

    @Schema(description = "请求体")
    private JsonNode requestBody;

    @Schema(description = "返参信息")
    private JsonNode responseBody;

}
