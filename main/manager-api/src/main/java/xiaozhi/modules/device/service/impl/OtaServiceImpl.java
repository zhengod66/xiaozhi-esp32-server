package xiaozhi.modules.device.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.modules.device.config.OtaConfig;
import xiaozhi.modules.device.constant.DeviceConstant;
import xiaozhi.modules.device.dto.ActivationCodeDTO;
import xiaozhi.modules.device.dto.DeviceDTO;
import xiaozhi.modules.device.dto.OtaRequestDTO;
import xiaozhi.modules.device.dto.OtaResponseDTO;
import xiaozhi.modules.device.service.AccessTokenService;
import xiaozhi.modules.device.service.ActivationCodeService;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.device.service.OtaService;

import java.util.Date;
import java.util.TimeZone;

/**
 * OTA服务实现类
 * 处理设备OTA请求，根据设备状态返回不同响应
 */
@Service
@AllArgsConstructor
@Slf4j
public class OtaServiceImpl implements OtaService {
    private static final String ACTIVATION_CODE_INSTRUCTION = "Please enter this activation code in the web app";

    private final DeviceService deviceService;
    private final ActivationCodeService activationCodeService;
    private final AccessTokenService accessTokenService;
    private final OtaConfig otaConfig;

    /**
     * 处理OTA请求
     *
     * @param request OTA请求DTO
     * @return OTA响应DTO
     */
    @Override
    public OtaResponseDTO processOtaRequest(OtaRequestDTO request) {
        log.info("收到设备OTA请求: {}", request);

        // 检查请求参数
        if (request.getMacAddress() == null || request.getMacAddress().isEmpty()) {
            log.error("设备MAC地址为空");
            return createEmptyResponse(request); // 返回包含基本信息的空响应
        }

        if (request.getClientId() == null || request.getClientId().isEmpty()) {
            log.error("设备UUID为空");
            return createEmptyResponse(request); // 返回包含基本信息的空响应
        }

        // 创建基础响应，包含服务器时间和空的固件信息
        OtaResponseDTO response = new OtaResponseDTO();
        
        // 添加服务器时间（确保timestamp是数字类型）
        response.addServerTime(System.currentTimeMillis(), 
                TimeZone.getDefault().getRawOffset() / (60 * 1000)); // 转换为分钟
        
        // 添加空的固件信息（即使没有更新也返回）
        String currentVersion = request.getFirmwareVersion() != null ? request.getFirmwareVersion() : "unknown";
        response.addFirmware(currentVersion, "");

        // 检查设备是否已注册
        DeviceDTO device = deviceService.getByMacAddress(request.getMacAddress());
        
        // 如果设备未注册，进行注册并生成激活码
        if (device == null) {
            log.info("设备未注册，MAC地址: {}", request.getMacAddress());
            OtaResponseDTO activationResponse = handleUnregisteredDevice(request);
            // 合并响应
            response.setActivation(activationResponse.getActivation());
            
            // 检查是否需要固件更新
            if (needsFirmwareUpdate(request.getDeviceType(), request.getFirmwareVersion())) {
                OtaResponseDTO firmwareResponse = getFirmwareUpdateInfo(request.getDeviceType());
                response.setFirmware(firmwareResponse.getFirmware());
            }
            
            return response;
        }
        
        // 根据设备状态处理请求
        switch (device.getStatus()) {
            case DeviceConstant.Status.INACTIVE:
                log.info("设备未激活, ID: {}", device.getId());
                OtaResponseDTO inactiveResponse = handleInactiveDevice(device.getId());
                response.setActivation(inactiveResponse.getActivation());
                break;
                
            case DeviceConstant.Status.WAITING:
                log.info("设备等待激活, ID: {}", device.getId());
                OtaResponseDTO waitingResponse = handleWaitingDevice(device.getId());
                response.setActivation(waitingResponse.getActivation());
                break;
                
            case DeviceConstant.Status.ACTIVE:
                log.info("设备已激活, ID: {}", device.getId());
                OtaResponseDTO activeResponse = handleActiveDevice(device.getId(), request.getMacAddress());
                response.setWebsocket(activeResponse.getWebsocket());
                break;
                
            default:
                log.warn("设备状态未知: {}", device.getStatus());
        }
        
        // 检查是否需要固件更新
        if (needsFirmwareUpdate(request.getDeviceType(), request.getFirmwareVersion())) {
            OtaResponseDTO firmwareResponse = getFirmwareUpdateInfo(request.getDeviceType());
            response.setFirmware(firmwareResponse.getFirmware());
        }
        
        return response;
    }

    /**
     * 创建包含基本信息的空响应
     */
    private OtaResponseDTO createEmptyResponse(OtaRequestDTO request) {
        OtaResponseDTO response = new OtaResponseDTO();
        
        // 添加服务器时间（确保timestamp是数字类型）
        response.addServerTime(System.currentTimeMillis(), 
                TimeZone.getDefault().getRawOffset() / (60 * 1000));
        
        // 添加空的固件信息
        String currentVersion = request.getFirmwareVersion() != null ? request.getFirmwareVersion() : "unknown";
        response.addFirmware(currentVersion, "");
        
        return response;
    }

    /**
     * 处理未注册设备
     *
     * @param request OTA请求DTO
     * @return OTA响应DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OtaResponseDTO handleUnregisteredDevice(OtaRequestDTO request) {
        log.info("注册新设备: {} - {}", request.getClientId(), request.getMacAddress());
        
        // 创建设备记录
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setMacAddress(request.getMacAddress());
        deviceDTO.setClientId(request.getClientId());
        deviceDTO.setType(request.getDeviceType());
        deviceDTO.setName(request.getDeviceName());
        deviceDTO.setStatus(DeviceConstant.Status.INACTIVE);
        
        // 保存设备
        deviceService.save(deviceDTO);
        
        // 获取设备ID (需要重新查询，因为save方法没有返回ID)
        DeviceDTO savedDevice = deviceService.getByMacAddress(request.getMacAddress());
        
        if (savedDevice == null) {
            throw new RuntimeException("设备保存失败");
        }
        
        // 生成激活码
        ActivationCodeDTO codeDTO = activationCodeService.generateCode(
                savedDevice.getId(), 
                DeviceConstant.DEFAULT_ACTIVATION_EXPIRE_MINUTES);
        
        // 构建响应
        return OtaResponseDTO.withActivation(
            codeDTO.getCode(), 
            ACTIVATION_CODE_INSTRUCTION
        );
    }

    /**
     * 处理未激活设备
     *
     * @param deviceId 设备ID
     * @return OTA响应DTO
     */
    @Override
    public OtaResponseDTO handleInactiveDevice(Long deviceId) {
        // 生成新的激活码
        ActivationCodeDTO codeDTO = activationCodeService.generateCode(
                deviceId, 
                DeviceConstant.DEFAULT_ACTIVATION_EXPIRE_MINUTES);
        
        return OtaResponseDTO.withActivation(
            codeDTO.getCode(), 
            ACTIVATION_CODE_INSTRUCTION
        );
    }

    /**
     * 处理等待激活设备
     *
     * @param deviceId 设备ID
     * @return OTA响应DTO
     */
    @Override
    public OtaResponseDTO handleWaitingDevice(Long deviceId) {
        // 获取最新的激活码
        ActivationCodeDTO codeDTO = activationCodeService.getValidCodeByDeviceId(deviceId);
        
        if (codeDTO == null || codeDTO.getStatus() != DeviceConstant.ActivationStatus.VALID) {
            // 如果没有有效的激活码，生成一个新的
            codeDTO = activationCodeService.generateCode(
                    deviceId, 
                    DeviceConstant.DEFAULT_ACTIVATION_EXPIRE_MINUTES);
        }
        
        return OtaResponseDTO.withActivation(
            codeDTO.getCode(), 
            ACTIVATION_CODE_INSTRUCTION
        );
    }

    /**
     * 处理已激活设备
     *
     * @param deviceId 设备ID
     * @param macAddress 设备MAC地址
     * @return OTA响应DTO
     */
    @Override
    public OtaResponseDTO handleActiveDevice(Long deviceId, String macAddress) {
        // 检查是否有有效的令牌，如果没有则生成新令牌
        String token = accessTokenService.getOrCreateToken(deviceId);
        
        // 获取WebSocket连接信息
        String server = otaConfig.getServer();
        Integer port = otaConfig.getPort();
        String path = otaConfig.getPath();
        
        return OtaResponseDTO.withWebsocket(
            token, 
            server, 
            port, 
            path
        );
    }

    /**
     * 检查是否需要固件更新
     *
     * @param deviceType 设备类型
     * @param currentVersion 当前固件版本
     * @return 是否需要更新
     */
    @Override
    public boolean needsFirmwareUpdate(String deviceType, String currentVersion) {
        // TODO: 实现固件版本检查逻辑
        // 目前仅为占位符
        return false;
    }

    /**
     * 获取固件更新信息
     *
     * @param deviceType 设备类型
     * @return OTA响应DTO (仅包含固件信息)
     */
    @Override
    public OtaResponseDTO getFirmwareUpdateInfo(String deviceType) {
        // TODO: 从数据库或配置中获取最新的固件信息
        // 目前仅为占位符
        return OtaResponseDTO.withFirmware(
            "1.0.6", 
            "https://firmware.example.com/v1.0.6.bin"
        );
    }
} 