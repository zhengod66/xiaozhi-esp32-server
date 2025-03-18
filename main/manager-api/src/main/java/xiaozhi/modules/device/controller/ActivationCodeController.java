package xiaozhi.modules.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.api.ApiResponse;
import xiaozhi.common.page.PageData;
import xiaozhi.modules.device.constant.DeviceConstant;
import xiaozhi.modules.device.dto.ActivationCodeDTO;
import xiaozhi.modules.device.service.ActivationCodeService;

import java.util.HashMap;
import java.util.Map;

/**
 * 激活码控制器 - RESTful API
 */
@RestController
@RequestMapping("/api/v1/activation")
@Tag(name = "激活码管理")
@AllArgsConstructor
public class ActivationCodeController {

    private final ActivationCodeService activationCodeService;

    /**
     * 激活码请求DTO
     */
    @Data
    public static class ActivationRequest {
        private String code;
        private Long userId;
        private Long deviceId;
    }

    /**
     * 获取激活码列表 - GET
     */
    @GetMapping
    @Operation(summary = "获取激活码列表")
    public ResponseEntity<ApiResponse<PageData<ActivationCodeDTO>>> getActivationCodes(
            @RequestParam(required = false) Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        PageData<ActivationCodeDTO> page = activationCodeService.page(params);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 获取单个激活码 - GET
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取单个激活码")
    public ResponseEntity<ApiResponse<ActivationCodeDTO>> getActivationCode(@PathVariable Long id) {
        ActivationCodeDTO code = activationCodeService.get(id);
        if (code == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "激活码不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(code));
    }

    /**
     * 生成激活码 - POST
     */
    @PostMapping
    @Operation(summary = "生成激活码")
    public ResponseEntity<ApiResponse<ActivationCodeDTO>> createActivationCode(
            @RequestBody ActivationRequest request) {
        if (request.getDeviceId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("设备ID不能为空"));
        }
        
        ActivationCodeDTO code = activationCodeService.generateCode(
                request.getDeviceId(), 
                DeviceConstant.DEFAULT_ACTIVATION_EXPIRE_MINUTES);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("激活码生成成功", code));
    }

    /**
     * 验证激活码 - GET
     */
    @GetMapping("/validate/{code}")
    @Operation(summary = "验证激活码")
    public ResponseEntity<ApiResponse<ActivationCodeDTO>> validateActivationCode(
            @PathVariable String code) {
        ActivationCodeDTO result = activationCodeService.validateCode(code);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("无效或已过期的激活码"));
        }
        return ResponseEntity.ok(ApiResponse.success("激活码有效", result));
    }

    /**
     * 使用激活码 - PUT
     */
    @PutMapping("/use")
    @Operation(summary = "使用激活码")
    public ResponseEntity<ApiResponse<Void>> useActivationCode(
            @RequestBody ActivationRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("激活码不能为空"));
        }
        
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户ID不能为空"));
        }
        
        boolean success = activationCodeService.useCode(request.getCode(), request.getUserId());
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("无效或已过期的激活码"));
        }
        
        return ResponseEntity.ok(ApiResponse.success("激活成功", null));
    }

    /**
     * 删除激活码 - DELETE
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除激活码")
    public ResponseEntity<ApiResponse<Void>> deleteActivationCode(@PathVariable Long id) {
        // 先检查是否存在
        ActivationCodeDTO code = activationCodeService.get(id);
        if (code == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "激活码不存在"));
        }
        
        Long[] ids = {id};
        activationCodeService.delete(ids);
        
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
    
    /**
     * 批量删除激活码 - DELETE
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除激活码")
    public ResponseEntity<ApiResponse<Void>> batchDeleteActivationCodes(
            @RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ID列表不能为空"));
        }
        
        activationCodeService.delete(ids);
        
        return ResponseEntity.ok(ApiResponse.success("批量删除成功", null));
    }
} 