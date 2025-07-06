package com.brotherc.documentcenter.model.dto.apiinfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "api信息详情")
public class ApiInfoDTO {

    @Schema(description = "api信息id")
    private Long apiInfoId;

    @Schema(description = "接口名称(英文)")
    private String name;

    @Schema(description = "接口名称(中文)")
    private String cnName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "唯一标识")
    private String code;

    @Schema(description = "访问地址")
    private String host;

    @Schema(description = "请求方式")
    private String reqMethod;

    @Schema(description = "访问地址前缀")
    private String reqContextPath;

    @Schema(description = "访问地址")
    private String reqPath;

    @Schema(description = "请求参数")
    private ArrayNode queryParam;

    @Schema(description = "路径参数")
    private ArrayNode pathParam;

    @Schema(description = "请求体")
    private ArrayNode requestBody;

    @Schema(description = "返参信息")
    private ArrayNode responseBody;

    @Schema(description = "api分类id")
    private Long apiInfoCategoryId;

    @Schema(description = "状态，1：未发布，2：已发布")
    private Integer status;

    @Schema(description = "入参信息json，请求头、请求体、请求参数等，用于展示")
    private JsonNode reqParamDisplayJson;

    @Schema(description = "返参信息json，用于展示")
    private JsonNode returnInfoDisplayJson;

}
