package xiaozhi.modules.device.service;

import xiaozhi.common.service.CrudService;
import xiaozhi.modules.device.dto.DeviceDTO;
import xiaozhi.modules.device.entity.DeviceEntity;

/**
 * 设备服务接口
 */
public interface DeviceService extends CrudService<DeviceEntity, DeviceDTO> {
    
    /**
     * 根据MAC地址查询设备
     * @param macAddress MAC地址
     * @return 设备信息
     */
    DeviceDTO getByMacAddress(String macAddress);
    
    /**
     * 根据ClientID查询设备
     * @param clientId 客户端ID
     * @return 设备信息
     */
    DeviceDTO getByClientId(String clientId);
    
    /**
     * 更新设备状态
     * @param id 设备ID
     * @param status 新状态
     */
    void updateStatus(Long id, Integer status);
    
    /**
     * 更新设备绑定的用户
     * @param id 设备ID
     * @param userId 用户ID
     */
    void update(Long id, Long userId);
    
} 