package xiaozhi.modules.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录表单
 */
@Data
@Schema(description = "登录表单")
public class LoginDTO implements Serializable {

    @Schema(description = "用户名", required = true)
    @NotBlank(message = "{sysuser.username.require}")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "{sysuser.password.require}")
    private String password;

    @Schema(description = "验证码")
    private String captcha;

    @Schema(description = "唯一标识")
    private String uuid;

}