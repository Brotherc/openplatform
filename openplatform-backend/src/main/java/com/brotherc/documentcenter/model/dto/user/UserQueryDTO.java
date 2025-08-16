package com.brotherc.documentcenter.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户查询入参")
public class UserQueryDTO {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "状态，1：禁用，2：启用")
    private Integer status;

    @Schema(description = "昵称")
    private String nickname;

}
