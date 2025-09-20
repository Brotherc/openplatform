package com.brotherc.documentcenter.model.dto.developer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员修改开发者密码DTO")
public class DeveloperChangePasswordDTO {

    @NotNull(message = "开发者ID不能为空")
    @Schema(description = "开发者ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long developerId;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$",
             message = "新密码必须包含至少一个小写字母、一个大写字母和一个数字，可包含特殊字符@$!%*?&")
    @Schema(description = "新密码（6-20个字符，必须包含大小写字母和数字）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

}
