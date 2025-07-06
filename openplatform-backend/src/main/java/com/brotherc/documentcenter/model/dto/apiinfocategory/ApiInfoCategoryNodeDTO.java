package com.brotherc.documentcenter.model.dto.apiinfocategory;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "api分类节点")
public class ApiInfoCategoryNodeDTO {

    @JsonProperty("key")
    @Schema(description = "api分类id")
    private Long apiInfoCategoryId;

    @JsonProperty("title")
    @Schema(description = "api分类名称")
    private String name;

    @JsonProperty("parentKey")
    @Schema(description = "父分类id")
    private Long parentId;

    @Schema(description = "类型，1：分类，2：api")
    private Integer type;

    @Schema(description = "状态，1：未发布，2：已发布")
    private Integer status;

    @Schema(description = "子节点")
    private List<ApiInfoCategoryNodeDTO> children = new ArrayList<>();

}
