package com.brotherc.documentcenter.model.dto.developer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "开发者查询入参")
public class DeveloperQueryDTO {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "状态，1：禁用，2：启用")
    private Integer status;

    @Schema(description = "开发者类型，1：个人，2：企业")
    private Integer developerType;

    @Schema(description = "认证状态，1：已认证，2：未认证，3：审核中，4：认证未通过")
    private Integer authenticateStatus;

}
