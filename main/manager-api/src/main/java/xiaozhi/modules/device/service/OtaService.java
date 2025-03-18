package xiaozhi.modules.device.service;

import xiaozhi.modules.device.dto.OtaRequestDTO;
import xiaozhi.modules.device.dto.OtaResponseDTO;

/**
 * OTA服务接口
 * 处理设备OTA请求，根据设备状态返回不同响应
 */
public interface OtaService {
    
    /**
     * 处理OTA请求
     * 
     * @param request OTA请求DTO
     * @return OTA响应DTO
     */
    OtaResponseDTO processOtaRequest(OtaRequestDTO request);
    
    /**
     * 处理未注册设备
     * 注册设备并生成激活码
     * 
     * @param request OTA请求DTO
     * @return OTA响应DTO
     */
    OtaResponseDTO handleUnregisteredDevice(OtaRequestDTO request);
    
    /**
     * 处理未激活设备
     * 生成新的激活码
     * 
     * @param deviceId 设备ID
     * @return OTA响应DTO
     */
    OtaResponseDTO handleInactiveDevice(Long deviceId);
    
    /**
     * 处理等待激活设备
     * 返回现有激活码
     * 
     * @param deviceId 设备ID
     * @return OTA响应DTO
     */
    OtaResponseDTO handleWaitingDevice(Long deviceId);
    
    /**
     * 处理已激活设备
     * 生成或返回访问令牌
     * 
     * @param deviceId 设备ID
     * @param macAddress 设备MAC地址
     * @return OTA响应DTO
     */
    OtaResponseDTO handleActiveDevice(Long deviceId, String macAddress);
    
    /**
     * 检查是否需要固件更新
     * 
     * @param deviceType 设备类型
     * @param currentVersion 当前固件版本
     * @return 是否需要更新
     */
    boolean needsFirmwareUpdate(String deviceType, String currentVersion);
    
    /**
     * 获取固件更新信息
     * 
     * @param deviceType 设备类型
     * @return OTA响应DTO (仅包含固件信息)
     */
    OtaResponseDTO getFirmwareUpdateInfo(String deviceType);
} 