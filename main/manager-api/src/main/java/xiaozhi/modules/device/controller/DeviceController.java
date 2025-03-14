package xiaozhi.modules.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.page.PageData;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.device.constant.DeviceConstant;
import xiaozhi.modules.device.dto.DeviceDTO;
import xiaozhi.modules.device.service.DeviceService;

import java.util.Date;
import java.util.Map;

/**
 * 设备控制器
 */
@RestController
@RequestMapping("/device")
@Tag(name = "设备管理")
@AllArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("page")
    @Operation(summary = "设备分页查询")
    public Result<PageData<DeviceDTO>> page(@RequestParam Map<String, Object> params) {
        PageData<DeviceDTO> page = deviceService.page(params);
        return new Result<PageData<DeviceDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "获取设备详情")
    public Result<DeviceDTO> get(@PathVariable("id") Long id) {
        DeviceDTO device = deviceService.get(id);
        return new Result<DeviceDTO>().ok(device);
    }

    @PostMapping
    @Operation(summary = "注册设备")
    public Result register(@RequestBody DeviceDTO dto) {
        // 验证必要参数
        if (dto.getMacAddress() == null || dto.getMacAddress().isEmpty()) {
            return new Result().error("MAC地址不能为空");
        }
        
        if (dto.getClientId() == null || dto.getClientId().isEmpty()) {
            return new Result().error("客户端ID不能为空");
        }

        // 检查重复设备
        DeviceDTO existDevice = deviceService.getByMacAddress(dto.getMacAddress());
        if (existDevice != null) {
            return new Result().error("该MAC地址已注册");
        }

        existDevice = deviceService.getByClientId(dto.getClientId());
        if (existDevice != null) {
            return new Result().error("该客户端ID已注册");
        }

        // 设置初始状态
        dto.setStatus(DeviceConstant.Status.INACTIVE);
        dto.setUpdateTime(new Date());
        
        // 保存设备
        deviceService.save(dto);
        
        return new Result().ok(dto);
    }

    @PutMapping
    @Operation(summary = "更新设备")
    public Result update(@RequestBody DeviceDTO dto) {
        // 验证必要参数
        if (dto.getId() == null) {
            return new Result().error("设备ID不能为空");
        }
        
        dto.setUpdateTime(new Date());
        deviceService.update(dto);
        
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除设备")
    public Result delete(@RequestBody Long[] ids) {
        deviceService.delete(ids);
        return new Result();
    }
} 