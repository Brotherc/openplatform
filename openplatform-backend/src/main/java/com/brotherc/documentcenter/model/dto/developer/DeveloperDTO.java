package com.brotherc.documentcenter.model.dto.developer;

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
@Schema(description = "开发者详情")
public class DeveloperDTO {

    @Schema(description = "开发者ID")
    private Long developerId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "状态，1：禁用，2：启用")
    private Integer status;

    @Schema(description = "开发者类型，1：个人，2：企业")
    private Integer developerType;

    @Schema(description = "认证状态，1：已认证，2：未认证，3：审核中，4：认证未通过")
    private Integer authenticateStatus;

    @Schema(description = "创建用户")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新用户")
    private Long updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
