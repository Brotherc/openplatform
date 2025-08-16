package com.brotherc.documentcenter.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户更新入参")
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$",
             message = "密码必须包含至少一个小写字母、一个大写字母和一个数字，可包含特殊字符@$!%*?&")
    @Schema(description = "密码（6-20个字符，必须包含大小写字母和数字，为空则不更新密码）")
    private String password;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态，1：禁用，2：启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Size(min = 2, max = 50, message = "昵称长度必须在2-50个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\s]+$", message = "昵称只能包含中文、字母、数字、下划线和空格")
    @Schema(description = "昵称（2-50个字符，可包含中文、字母、数字、下划线和空格）")
    private String nickname;

}
