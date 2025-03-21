package xiaozhi.modules.device.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.modules.device.config.OtaConfig;
import xiaozhi.modules.device.dto.OtaRequestDTO;
import xiaozhi.modules.device.dto.OtaResponseDTO;
import xiaozhi.modules.device.service.OtaService;

/**
 * OTA控制器
 * 处理设备OTA请求，返回设备状态信息
 */
@RestController
@RequestMapping("${xiaozhi.ws.otaPath:/xiaozhi/ota}")
@Tag(name = "OTA接口")
@AllArgsConstructor
@Slf4j
public class OtaController {

    private final OtaService otaService;
    private final OtaConfig otaConfig;
    private final ObjectMapper objectMapper;

    /**
     * 处理设备OTA请求
     * 根据设备状态返回不同响应：
     * 1. 未注册设备：注册并生成激活码
     * 2. 未激活设备：返回激活码
     * 3. 等待激活设备：返回激活码
     * 4. 已激活设备：返回访问令牌和WebSocket信息
     * 5. 需要固件更新：返回固件信息
     *
     * @param request OTA请求DTO
     * @return OTA响应对象
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @Operation(summary = "设备OTA请求")
    @LogOperation("设备OTA请求")
    public OtaResponseDTO handleOtaRequest(@RequestBody OtaRequestDTO request) {
        log.info("接收到OTA请求: {}", request);
        
        try {
            // 调用服务处理OTA请求
            return otaService.processOtaRequest(request);
        } catch (Exception e) {
            log.error("处理OTA请求时发生错误", e);
            // 返回空响应
            OtaResponseDTO emptyResponse = new OtaResponseDTO();
            // 确保即使发生错误，仍然添加服务器时间和空的固件信息
            emptyResponse.addServerTime(System.currentTimeMillis(), 
                    java.util.TimeZone.getDefault().getRawOffset() / (60 * 1000));
            
            // 添加空的固件信息
            String currentVersion = request != null && request.getFirmwareVersion() != null 
                    ? request.getFirmwareVersion() : "unknown";
            emptyResponse.addFirmware(currentVersion, "");
            
            return emptyResponse;
        }
    }
} 