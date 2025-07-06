package com.brotherc.documentcenter.model.dto.doccatalog;

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
@Schema(description = "文章目录节点")
public class DocCatalogNodeDTO {

    @JsonProperty("key")
    @Schema(description = "文章目录id")
    private Long docCatalogId;

    @JsonProperty("title")
    @Schema(description = "文章目录名称")
    private String name;

    @Schema(description = "分组id")
    private Long docCatalogGroupId;

    @JsonProperty("parentKey")
    @Schema(description = "父目录id")
    private Long parentId;

    @Schema(description = "类型，1：目录，2：文章")
    private Integer type;

    @Schema(description = "状态，1：未发布，2：已发布")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "子节点")
    private List<DocCatalogNodeDTO> children = new ArrayList<>();

}
