package com.brotherc.documentcenter.model.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "文章详情")
public class DocumentDTO {

    @Schema(description = "文章id")
    private Long id;

    @Schema(description = "文章名称")
    private String name;

    @Schema(description = "分组id")
    private Long docCatalogGroupId;

    @Schema(description = "父目录id")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "文章内容")
    private String content;

}
