package xiaozhi.modules.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.page.PageData;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.device.constant.DeviceConstant;
import xiaozhi.modules.device.dto.ActivationCodeDTO;
import xiaozhi.modules.device.service.ActivationCodeService;

import java.util.Map;

/**
 * 激活码控制器
 */
@RestController
@RequestMapping("/activation")
@Tag(name = "激活码管理")
@AllArgsConstructor
public class ActivationCodeController {

    private final ActivationCodeService activationCodeService;

    @GetMapping("page")
    @Operation(summary = "激活码分页查询")
    public Result<PageData<ActivationCodeDTO>> page(@RequestParam Map<String, Object> params) {
        PageData<ActivationCodeDTO> page = activationCodeService.page(params);
        return new Result<PageData<ActivationCodeDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "获取激活码详情")
    public Result<ActivationCodeDTO> get(@PathVariable("id") Long id) {
        ActivationCodeDTO code = activationCodeService.get(id);
        return new Result<ActivationCodeDTO>().ok(code);
    }

    @PostMapping("generate/{deviceId}")
    @Operation(summary = "生成激活码")
    public Result<ActivationCodeDTO> generate(@PathVariable("deviceId") Long deviceId) {
        ActivationCodeDTO code = activationCodeService.generateCode(deviceId, DeviceConstant.DEFAULT_ACTIVATION_EXPIRE_MINUTES);
        return new Result<ActivationCodeDTO>().ok(code);
    }

    @PostMapping("validate/{code}")
    @Operation(summary = "验证激活码")
    public Result<ActivationCodeDTO> validate(@PathVariable("code") String code) {
        ActivationCodeDTO result = activationCodeService.validateCode(code);
        if (result == null) {
            return new Result<ActivationCodeDTO>().error("无效或已过期的激活码");
        }
        return new Result<ActivationCodeDTO>().ok(result);
    }

    @PostMapping("use")
    @Operation(summary = "使用激活码")
    public Result use(@RequestParam("code") String code, @RequestParam("userId") Long userId) {
        boolean success = activationCodeService.useCode(code, userId);
        if (!success) {
            return new Result().error("无效或已过期的激活码");
        }
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除激活码")
    public Result delete(@RequestBody Long[] ids) {
        activationCodeService.delete(ids);
        return new Result();
    }
} 