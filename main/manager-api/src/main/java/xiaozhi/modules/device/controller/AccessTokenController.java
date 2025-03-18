package xiaozhi.modules.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.page.PageData;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.device.constant.DeviceConstant;
import xiaozhi.modules.device.dto.AccessTokenDTO;
import xiaozhi.modules.device.service.AccessTokenService;

import java.util.Map;

/**
 * 訪問令牌控制器
 */
@RestController
@RequestMapping("/token")
@Tag(name = "設備訪問令牌")
@AllArgsConstructor
public class AccessTokenController {

    private final AccessTokenService accessTokenService;

    @GetMapping("page")
    @Operation(summary = "令牌分頁查詢")
    public Result<PageData<AccessTokenDTO>> page(@RequestParam Map<String, Object> params) {
        PageData<AccessTokenDTO> page = accessTokenService.page(params);
        return new Result<PageData<AccessTokenDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "獲取令牌詳情")
    public Result<AccessTokenDTO> get(@PathVariable("id") Long id) {
        AccessTokenDTO token = accessTokenService.get(id);
        return new Result<AccessTokenDTO>().ok(token);
    }

    @PostMapping("generate/{deviceId}")
    @Operation(summary = "生成令牌")
    public Result<AccessTokenDTO> generate(@PathVariable("deviceId") Long deviceId) {
        AccessTokenDTO token = accessTokenService.generateToken(deviceId, DeviceConstant.DEFAULT_TOKEN_EXPIRE_HOURS);
        return new Result<AccessTokenDTO>().ok(token);
    }

    @PostMapping("validate")
    @Operation(summary = "驗證令牌")
    public Result<AccessTokenDTO> validate(@RequestParam("token") String token) {
        AccessTokenDTO result = accessTokenService.validateToken(token);
        if (result == null) {
            return new Result<AccessTokenDTO>().error("無效或已過期的令牌");
        }
        return new Result<AccessTokenDTO>().ok(result);
    }

    @PostMapping("revoke/{id}")
    @Operation(summary = "撤銷令牌")
    public Result revoke(@PathVariable("id") Long id) {
        accessTokenService.revokeToken(id);
        return new Result();
    }

    @PostMapping("revoke/device/{deviceId}")
    @Operation(summary = "撤銷設備所有令牌")
    public Result revokeAll(@PathVariable("deviceId") Long deviceId) {
        accessTokenService.revokeAllTokensByDevice(deviceId);
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "刪除令牌記錄")
    public Result delete(@RequestBody Long[] ids) {
        accessTokenService.delete(ids);
        return new Result();
    }
} 