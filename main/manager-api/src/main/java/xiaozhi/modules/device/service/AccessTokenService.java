package xiaozhi.modules.device.service;

import xiaozhi.common.service.CrudService;
import xiaozhi.modules.device.dto.AccessTokenDTO;
import xiaozhi.modules.device.entity.AccessTokenEntity;

/**
 * 访问令牌服务接口
 */
public interface AccessTokenService extends CrudService<AccessTokenEntity, AccessTokenDTO> {
    
    /**
     * 生成设备访问令牌
     * @param deviceId 设备ID
     * @param expireHours 过期时间（小时）
     * @return 访问令牌DTO
     */
    AccessTokenDTO generateToken(Long deviceId, int expireHours);
    
    /**
     * 验证令牌
     * @param token 令牌
     * @return 访问令牌DTO，如果无效则返回null
     */
    AccessTokenDTO validateToken(String token);
    
    /**
     * 撤销令牌
     * @param id 令牌ID
     */
    void revokeToken(Long id);
    
    /**
     * 撤销设备的所有令牌
     * @param deviceId 设备ID
     */
    void revokeAllTokensByDevice(Long deviceId);
    
    /**
     * 清理過期令牌
     * 
     * @return 清理的令牌數量
     */
    int cleanExpiredTokens();
    
    /**
     * 獲取設備的有效訪問令牌
     * 如果有多個有效令牌，返回過期時間最晚的一個
     * 
     * @param deviceId 設備ID
     * @return 有效訪問令牌，如果沒有則返回null
     */
    AccessTokenDTO getValidTokenByDeviceId(Long deviceId);
    
    /**
     * 获取或创建设备访问令牌
     * 如果设备已有有效令牌，则返回该令牌；否则创建新令牌
     * 
     * @param deviceId 设备ID
     * @return 访问令牌字符串
     */
    String getOrCreateToken(Long deviceId);
} 