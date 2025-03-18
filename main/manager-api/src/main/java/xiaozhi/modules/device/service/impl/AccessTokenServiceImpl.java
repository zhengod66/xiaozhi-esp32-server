package xiaozhi.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.service.impl.CrudServiceImpl;
import xiaozhi.modules.device.constant.DeviceConstant;
import xiaozhi.modules.device.dao.AccessTokenDao;
import xiaozhi.modules.device.dto.AccessTokenDTO;
import xiaozhi.modules.device.dto.DeviceDTO;
import xiaozhi.modules.device.entity.AccessTokenEntity;
import xiaozhi.modules.device.service.AccessTokenService;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.security.jwt.JwtTokenProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 訪問令牌服務實現類
 */
@Service
@AllArgsConstructor
@Slf4j
public class AccessTokenServiceImpl extends CrudServiceImpl<AccessTokenDao, AccessTokenEntity, AccessTokenDTO> implements AccessTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtils redisUtils;
    private final DeviceService deviceService;

    /**
     * 生成設備訪問令牌
     *
     * @param deviceId    設備ID
     * @param expireHours 過期時間（小時）
     * @return 訪問令牌DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessTokenDTO generateToken(Long deviceId, int expireHours) {
        // 檢查設備是否存在
        DeviceDTO deviceDTO = deviceService.get(deviceId);
        if (deviceDTO == null) {
            throw new RuntimeException("設備不存在");
        }

        // 檢查設備狀態是否為已激活
        if (deviceDTO.getStatus() != DeviceConstant.Status.ACTIVE) {
            throw new RuntimeException("設備未激活，無法生成令牌");
        }

        // 撤銷該設備之前的所有令牌
        revokeAllTokensByDevice(deviceId);

        // 計算過期時間
        Date expireTime = calculateExpireTime(expireHours);

        // 生成JWT令牌
        long expirationSeconds = expireHours * 3600L;
        String jwtToken = jwtTokenProvider.generateToken(deviceId, deviceDTO.getMacAddress(), expirationSeconds);

        // 創建訪問令牌實體並保存
        AccessTokenEntity entity = new AccessTokenEntity();
        entity.setDeviceId(deviceId);
        entity.setToken(jwtToken);
        entity.setIsRevoked(DeviceConstant.TokenRevoked.NO);
        entity.setExpireTime(expireTime);

        baseDao.insert(entity);

        // 緩存令牌
        cacheToken(entity);

        return convertEntity(entity);
    }

    /**
     * 驗證令牌
     *
     * @param token 令牌
     * @return 訪問令牌DTO，如果無效則返回null
     */
    @Override
    public AccessTokenDTO validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        // 首先驗證JWT令牌的簽名和過期時間
        if (!jwtTokenProvider.validateToken(token)) {
            return null;
        }

        // 檢查令牌是否已被撤銷
        if (isTokenRevoked(token)) {
            return null;
        }

        // 從數據庫獲取令牌信息
        AccessTokenEntity entity = getEntityByToken(token);
        if (entity == null) {
            return null;
        }

        // 檢查是否過期
        if (entity.getExpireTime().before(new Date())) {
            // 標記為已過期
            revokeToken(entity.getId());
            return null;
        }

        return convertEntity(entity);
    }

    /**
     * 撤銷令牌
     *
     * @param id 令牌ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeToken(Long id) {
        AccessTokenEntity entity = baseDao.selectById(id);
        if (entity != null && entity.getIsRevoked() == DeviceConstant.TokenRevoked.NO) {
            // 更新數據庫
            entity.setIsRevoked(DeviceConstant.TokenRevoked.YES);
            baseDao.updateById(entity);

            // 將令牌添加到已撤銷令牌集合
            addToRevokedTokens(entity.getToken());

            // 從設備活躍令牌緩存中刪除
            removeFromActiveTokens(entity.getDeviceId());
        }
    }

    /**
     * 撤銷設備的所有令牌
     *
     * @param deviceId 設備ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeAllTokensByDevice(Long deviceId) {
        List<AccessTokenEntity> tokens = baseDao.selectList(
                new LambdaQueryWrapper<AccessTokenEntity>()
                        .eq(AccessTokenEntity::getDeviceId, deviceId)
                        .eq(AccessTokenEntity::getIsRevoked, DeviceConstant.TokenRevoked.NO)
        );

        if (!tokens.isEmpty()) {
            // 批量更新數據庫
            baseDao.update(null, new LambdaUpdateWrapper<AccessTokenEntity>()
                    .eq(AccessTokenEntity::getDeviceId, deviceId)
                    .eq(AccessTokenEntity::getIsRevoked, DeviceConstant.TokenRevoked.NO)
                    .set(AccessTokenEntity::getIsRevoked, DeviceConstant.TokenRevoked.YES));

            // 將所有令牌添加到已撤銷令牌集合
            for (AccessTokenEntity token : tokens) {
                addToRevokedTokens(token.getToken());
            }

            // 從設備活躍令牌緩存中刪除
            removeFromActiveTokens(deviceId);
        }
    }

    /**
     * 清理過期令牌
     * 
     * @return 清理的令牌數量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredTokens() {
        Date now = new Date();
        
        // 查詢所有已過期但未撤銷的令牌
        List<AccessTokenEntity> expiredTokens = baseDao.selectList(
                new LambdaQueryWrapper<AccessTokenEntity>()
                        .eq(AccessTokenEntity::getIsRevoked, DeviceConstant.TokenRevoked.NO)
                        .lt(AccessTokenEntity::getExpireTime, now)
        );

        int count = expiredTokens.size();
        if (count > 0) {
            log.info("發現{}個過期令牌需要清理", count);
            
            for (AccessTokenEntity token : expiredTokens) {
                // 撤銷令牌
                revokeToken(token.getId());
            }
        } else {
            log.info("沒有發現過期令牌");
        }
        
        return count;
    }

    @Override
    public com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AccessTokenEntity> getWrapper(Map<String, Object> params) {
        // 創建查詢包裝器
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AccessTokenEntity> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        // 根據參數添加查詢條件
        if (params != null) {
            if (params.containsKey("token")) {
                wrapper.eq("token", params.get("token"));
            }
            
            if (params.containsKey("deviceId")) {
                wrapper.eq("device_id", params.get("deviceId"));
            }
            
            if (params.containsKey("isRevoked")) {
                wrapper.eq("is_revoked", params.get("isRevoked"));
            }
        }
        
        // 添加默認排序
        wrapper.orderByDesc("create_date");
        
        return wrapper;
    }
    
    /**
     * 獲取設備的有效訪問令牌
     * 如果有多個有效令牌，返回過期時間最晚的一個
     *
     * @param deviceId 設備ID
     * @return 有效訪問令牌，如果沒有則返回null
     */
    @Override
    public AccessTokenDTO getValidTokenByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        
        Date now = new Date();
        
        // 查詢該設備未撤銷且未過期的令牌，按過期時間降序排序
        List<AccessTokenEntity> tokens = baseDao.selectList(
                new LambdaQueryWrapper<AccessTokenEntity>()
                        .eq(AccessTokenEntity::getDeviceId, deviceId)
                        .eq(AccessTokenEntity::getIsRevoked, DeviceConstant.TokenRevoked.NO)
                        .gt(AccessTokenEntity::getExpireTime, now)
                        .orderByDesc(AccessTokenEntity::getExpireTime)
                        .last("LIMIT 1")
        );
        
        if (tokens.isEmpty()) {
            log.info("設備無有效訪問令牌，設備ID: {}", deviceId);
            return null;
        }
        
        AccessTokenEntity entity = tokens.get(0);
        log.info("獲取到設備有效訪問令牌，設備ID: {}, 令牌ID: {}", deviceId, entity.getId());
        
        return convertEntity(entity);
    }

    /**
     * 获取或创建设备访问令牌
     * 如果设备已有有效令牌，则返回该令牌；否则创建新令牌
     * 
     * @param deviceId 设备ID
     * @return 访问令牌字符串
     */
    @Override
    public String getOrCreateToken(Long deviceId) {
        // 首先尝试获取已有的有效令牌
        AccessTokenDTO validToken = getValidTokenByDeviceId(deviceId);
        
        if (validToken != null) {
            log.info("使用现有访问令牌，设备ID: {}", deviceId);
            return validToken.getToken();
        }
        
        // 如果没有有效令牌，生成新的
        AccessTokenDTO newToken = generateToken(deviceId, DeviceConstant.DEFAULT_TOKEN_EXPIRE_HOURS);
        log.info("生成新的访问令牌，设备ID: {}", deviceId);
        
        return newToken.getToken();
    }

    // ========== 輔助方法 ==========
    
    /**
     * 計算過期時間
     */
    private Date calculateExpireTime(int expireHours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expireHours);
        return calendar.getTime();
    }
    
    /**
     * 緩存令牌
     */
    private void cacheToken(AccessTokenEntity entity) {
        // 緩存設備的活躍令牌，在驗證時快速查找
        String activeTokenKey = RedisKeys.getDeviceActiveTokenKey(entity.getDeviceId());
        redisUtils.set(activeTokenKey, entity.getToken());
        
        // 設置緩存過期時間比令牌過期時間稍長一些
        long ttlMillis = entity.getExpireTime().getTime() - System.currentTimeMillis() + 60000; // 額外1分鐘
        if (ttlMillis > 0) {
            redisUtils.expire(activeTokenKey, ttlMillis / 1000);
        }
    }
    
    /**
     * 檢查令牌是否已被撤銷
     */
    private boolean isTokenRevoked(String token) {
        return redisUtils.sIsMember(RedisKeys.getRevokedTokensKey(), token);
    }
    
    /**
     * 將令牌添加到已撤銷令牌集合
     */
    private void addToRevokedTokens(String token) {
        redisUtils.sAdd(RedisKeys.getRevokedTokensKey(), token);
        
        // 令牌最長有效期為一周，因此我們可以設置緩存過期時間為一周
        redisUtils.expire(RedisKeys.getRevokedTokensKey(), 7 * 24 * 3600);
    }
    
    /**
     * 從設備活躍令牌緩存中刪除
     */
    private void removeFromActiveTokens(Long deviceId) {
        redisUtils.delete(RedisKeys.getDeviceActiveTokenKey(deviceId));
    }
    
    /**
     * 根據令牌查詢實體
     */
    private AccessTokenEntity getEntityByToken(String token) {
        return baseDao.selectOne(
                new LambdaQueryWrapper<AccessTokenEntity>()
                        .eq(AccessTokenEntity::getToken, token)
                        .eq(AccessTokenEntity::getIsRevoked, DeviceConstant.TokenRevoked.NO)
        );
    }
    
    /**
     * 將實體轉換為DTO
     */
    private AccessTokenDTO convertEntity(AccessTokenEntity entity) {
        if (entity == null) {
            return null;
        }
        AccessTokenDTO dto = new AccessTokenDTO();
        org.springframework.beans.BeanUtils.copyProperties(entity, dto);
        
        // 如果需要，還可以填充設備信息
        if (entity.getDeviceId() != null) {
            dto.setDevice(deviceService.get(entity.getDeviceId()));
        }
        
        return dto;
    }
} 