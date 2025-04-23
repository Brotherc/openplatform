package com.brotherc.documentcenter.model.dto.doccataloggroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "分组详情")
public class DocCatalogGroupDTO {

    @Schema(description = "分组id")
    private Long docCatalogGroupId;

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态，1：未发布，2：已发布")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建用户")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新用户")
    private Long updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-否，1-是")
    private Integer isDel;

}
