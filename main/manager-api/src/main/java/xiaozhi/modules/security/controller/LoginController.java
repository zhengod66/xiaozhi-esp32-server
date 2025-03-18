package xiaozhi.modules.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.utils.PropertiesUtils;
import xiaozhi.common.utils.Result;
import xiaozhi.common.validator.AssertUtils;
import xiaozhi.modules.security.dto.LoginDTO;
import xiaozhi.modules.security.password.PasswordUtils;
import xiaozhi.modules.security.service.CaptchaService;
import xiaozhi.modules.security.service.SysUserTokenService;
import xiaozhi.modules.sys.dto.SysUserDTO;
import xiaozhi.modules.sys.service.SysParamsService;
import xiaozhi.modules.sys.service.SysUserService;

import java.io.IOException;

/**
 * 登录
 */
@AllArgsConstructor
@RestController
@Tag(name = "登录管理")
public class LoginController {
    private final SysUserService sysUserService;
    private final SysUserTokenService sysUserTokenService;
    private final CaptchaService captchaService;
    private final RedisUtils redisUtils;
    private final SysParamsService sysParamsService;
    private final PropertiesUtils propertiesUtils;

    @GetMapping("captcha")
    @Operation(summary = "验证码")
    public void captcha(HttpServletResponse response, String uuid) throws IOException {
        //uuid不能为空
        AssertUtils.isBlank(uuid, ErrorCode.IDENTIFIER_NOT_NULL);

        //生成验证码
        captchaService.create(response, uuid);
    }

    @PostMapping("login")
    @Operation(summary = "登录")
    public Result login(HttpServletRequest request, @RequestBody LoginDTO login) {
        // 简化登录逻辑，移除验证码验证
        
        // 根据用户名获取用户
        SysUserDTO user = sysUserService.getByUsername(login.getUsername());
        if (user == null) {
            return new Result().error(ErrorCode.ACCOUNT_PASSWORD_ERROR);
        }
        
        // 密码验证
        if (!PasswordUtils.matches(login.getPassword(), user.getPassword())) {
            return new Result().error(ErrorCode.ACCOUNT_PASSWORD_ERROR);
        }
        
        // 账号锁定
        if (user.getStatus() == 0) {
            return new Result().error(ErrorCode.ACCOUNT_DISABLE);
        }
        
        // 生成token
        return sysUserTokenService.createToken(user.getId());
    }

}