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
     * 清理过期令牌
     */
    void cleanExpiredTokens();
    
} 