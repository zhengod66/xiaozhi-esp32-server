package xiaozhi.modules.device.service;

import xiaozhi.common.service.CrudService;
import xiaozhi.modules.device.dto.ActivationCodeDTO;
import xiaozhi.modules.device.entity.ActivationCodeEntity;

/**
 * 激活码服务接口
 */
public interface ActivationCodeService extends CrudService<ActivationCodeEntity, ActivationCodeDTO> {
    
    /**
     * 生成激活码
     * @param deviceId 设备ID
     * @param expireMinutes 过期时间（分钟）
     * @return 激活码DTO
     */
    ActivationCodeDTO generateCode(Long deviceId, int expireMinutes);
    
    /**
     * 验证激活码
     * @param code 激活码
     * @return 激活码DTO，如果无效则返回null
     */
    ActivationCodeDTO validateCode(String code);
    
    /**
     * 使用激活码
     * @param code 激活码
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean useCode(String code, Long userId);
    
    /**
     * 清理过期激活码
     */
    void cleanExpiredCodes();
    
} 