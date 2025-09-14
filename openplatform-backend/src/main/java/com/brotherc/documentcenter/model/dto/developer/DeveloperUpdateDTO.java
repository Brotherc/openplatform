package com.brotherc.documentcenter.model.dto.developer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "开发者更新入参")
public class DeveloperUpdateDTO {

    @NotNull(message = "开发者ID不能为空")
    @Schema(description = "开发者ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long developerId;

    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$",
             message = "密码必须包含至少一个小写字母、一个大写字母和一个数字，可包含特殊字符@$!%*?&")
    @Schema(description = "密码（6-20个字符，必须包含大小写字母和数字，为空则不更新密码）")
    private String password;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态，1：禁用，2：启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @NotNull(message = "开发者类型不能为空")
    @Schema(description = "开发者类型，1：个人，2：企业", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer developerType;

    @NotNull(message = "认证状态不能为空")
    @Schema(description = "认证状态，1：已认证，2：未认证，3：审核中，4：认证未通过", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer authenticateStatus;

}
