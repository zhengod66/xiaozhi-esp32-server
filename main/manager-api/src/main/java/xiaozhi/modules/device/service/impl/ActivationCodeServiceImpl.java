package xiaozhi.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.service.impl.CrudServiceImpl;
import xiaozhi.modules.device.constant.DeviceConstant;
import xiaozhi.modules.device.dao.ActivationCodeDao;
import xiaozhi.modules.device.dto.ActivationCodeDTO;
import xiaozhi.modules.device.entity.ActivationCodeEntity;
import xiaozhi.modules.device.service.ActivationCodeService;
import xiaozhi.modules.device.service.DeviceService;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 激活码服务实现类
 */
@Service
@AllArgsConstructor
public class ActivationCodeServiceImpl extends CrudServiceImpl<ActivationCodeDao, ActivationCodeEntity, ActivationCodeDTO> implements ActivationCodeService {

    private final RedisUtils redisUtils;
    private final DeviceService deviceService;

    /**
     * 生成激活码
     *
     * @param deviceId      设备ID
     * @param expireMinutes 过期时间（分钟）
     * @return 激活码DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivationCodeDTO generateCode(Long deviceId, int expireMinutes) {
        // 检查设备是否存在
        if (deviceId == null || deviceService.get(deviceId) == null) {
            throw new RuntimeException("设备不存在");
        }

        // 失效之前的激活码
        invalidateExistingCodes(deviceId);

        // 生成新的激活码
        String code = generateUniqueCode();
        Date expireTime = calculateExpireTime(expireMinutes);

        // 创建激活码实体
        ActivationCodeEntity entity = new ActivationCodeEntity();
        entity.setCode(code);
        entity.setDeviceId(deviceId);
        entity.setStatus(DeviceConstant.ActivationStatus.VALID);
        entity.setExpireTime(expireTime);

        // 保存到数据库
        baseDao.insert(entity);

        // 保存到Redis缓存
        saveToRedis(entity);

        // 更新设备状态为等待激活
        deviceService.updateStatus(deviceId, DeviceConstant.Status.WAITING);

        return convertEntity(entity);
    }

    /**
     * 验证激活码
     *
     * @param code 激活码
     * @return 激活码DTO，如果无效则返回null
     */
    @Override
    public ActivationCodeDTO validateCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }

        // 首先从Redis缓存获取
        Map<String, Object> codeMap = redisUtils.hGetAll(RedisKeys.getActivationCodeKey(code));
        ActivationCodeDTO dto = null;

        if (codeMap != null && !codeMap.isEmpty()) {
            // 从Redis获取激活码信息
            dto = mapToDto(codeMap);
        } else {
            // 如果Redis中不存在，则从数据库查询
            ActivationCodeEntity entity = getByCode(code);
            if (entity != null) {
                dto = convertEntity(entity);
                // 将查询结果保存到Redis缓存
                saveToRedis(entity);
            }
        }

        // 检查激活码是否有效
        if (dto != null) {
            if (dto.getStatus() != DeviceConstant.ActivationStatus.VALID) {
                return null; // 激活码已使用或已过期
            }

            // 检查激活码是否过期
            if (dto.getExpireTime().before(new Date())) {
                // 标记为过期
                updateStatus(dto.getId(), DeviceConstant.ActivationStatus.EXPIRED);
                return null;
            }
        }

        return dto;
    }

    /**
     * 使用激活码
     *
     * @param code   激活码
     * @param userId 用户ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useCode(String code, Long userId) {
        ActivationCodeDTO dto = validateCode(code);
        if (dto == null) {
            return false;
        }

        // 更新激活码状态为已使用
        updateStatus(dto.getId(), DeviceConstant.ActivationStatus.USED);

        // 更新设备状态为已激活并绑定用户
        deviceService.updateStatus(dto.getDeviceId(), DeviceConstant.Status.ACTIVE);

        // 绑定设备到用户
        bindDeviceToUser(dto.getDeviceId(), userId);

        return true;
    }

    /**
     * 清理过期激活码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredCodes() {
        Date now = new Date();
        
        // 查询所有已过期但状态仍为有效的激活码
        List<ActivationCodeEntity> expiredCodes = baseDao.selectList(
                new LambdaQueryWrapper<ActivationCodeEntity>()
                        .eq(ActivationCodeEntity::getStatus, DeviceConstant.ActivationStatus.VALID)
                        .lt(ActivationCodeEntity::getExpireTime, now)
        );

        for (ActivationCodeEntity code : expiredCodes) {
            // 更新状态为已过期
            code.setStatus(DeviceConstant.ActivationStatus.EXPIRED);
            baseDao.updateById(code);
            
            // 从Redis中删除
            redisUtils.delete(RedisKeys.getActivationCodeKey(code.getCode()));
        }
    }

    @Override
    public com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ActivationCodeEntity> getWrapper(Map<String, Object> params) {
        // 创建查询包装器
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ActivationCodeEntity> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        // 您可以根据params中的条件添加查询条件
        // 例如：如果params中包含"code"参数，则添加code条件
        if (params != null && params.containsKey("code")) {
            wrapper.eq("code", params.get("code"));
        }
        
        // 添加默认排序
        wrapper.orderByDesc("create_date");
        
        return wrapper;
    }

    // ========== 辅助方法 ==========

    /**
     * 失效该设备之前的激活码
     */
    private void invalidateExistingCodes(Long deviceId) {
        List<ActivationCodeEntity> existingCodes = baseDao.selectList(
                new LambdaQueryWrapper<ActivationCodeEntity>()
                        .eq(ActivationCodeEntity::getDeviceId, deviceId)
                        .eq(ActivationCodeEntity::getStatus, DeviceConstant.ActivationStatus.VALID)
        );

        for (ActivationCodeEntity code : existingCodes) {
            code.setStatus(DeviceConstant.ActivationStatus.EXPIRED);
            baseDao.updateById(code);
            
            // 从Redis中删除
            redisUtils.delete(RedisKeys.getActivationCodeKey(code.getCode()));
        }
    }

    /**
     * 生成唯一的6位数字激活码
     */
    private String generateUniqueCode() {
        String code;
        boolean isUnique = false;
        
        do {
            // 生成6位随机数字
            int randomNum = ThreadLocalRandom.current().nextInt(100000, 1000000);
            code = String.valueOf(randomNum);
            
            // 检查是否已存在
            isUnique = getByCode(code) == null;
        } while (!isUnique);
        
        return code;
    }

    /**
     * 根据code查询激活码
     */
    private ActivationCodeEntity getByCode(String code) {
        return baseDao.selectOne(
                new LambdaQueryWrapper<ActivationCodeEntity>()
                        .eq(ActivationCodeEntity::getCode, code)
        );
    }

    /**
     * 计算过期时间
     */
    private Date calculateExpireTime(int expireMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expireMinutes);
        return calendar.getTime();
    }

    /**
     * 保存激活码到Redis
     */
    private void saveToRedis(ActivationCodeEntity entity) {
        if (entity == null) {
            return;
        }
        
        String key = RedisKeys.getActivationCodeKey(entity.getCode());
        Map<String, Object> map = new HashMap<>();
        
        map.put("id", entity.getId());
        map.put("code", entity.getCode());
        map.put("deviceId", entity.getDeviceId());
        map.put("status", entity.getStatus());
        map.put("expireTime", entity.getExpireTime().getTime());
        map.put("createDate", entity.getCreateDate().getTime());

        // 保存到Redis，设置过期时间
        redisUtils.hMSet(key, map);
        
        // 设置Redis过期时间比激活码过期时间稍长一些
        long ttl = (entity.getExpireTime().getTime() - System.currentTimeMillis()) + 60000; // 额外1分钟
        if (ttl > 0) {
            // 将毫秒转换为秒，因为RedisUtils.expire接受的是秒
            redisUtils.expire(key, ttl / 1000);
        }
    }

    /**
     * 更新激活码状态
     */
    private void updateStatus(Long id, int status) {
        ActivationCodeEntity entity = baseDao.selectById(id);
        if (entity != null) {
            entity.setStatus(status);
            baseDao.updateById(entity);
            
            // 如果是标记为过期或已使用，则从Redis中删除
            if (status != DeviceConstant.ActivationStatus.VALID) {
                redisUtils.delete(RedisKeys.getActivationCodeKey(entity.getCode()));
            } else {
                // 否则更新Redis缓存
                saveToRedis(entity);
            }
        }
    }

    /**
     * 将Redis的Map转换为DTO对象
     */
    private ActivationCodeDTO mapToDto(Map<String, Object> map) {
        ActivationCodeDTO dto = new ActivationCodeDTO();
        dto.setId(Long.parseLong(map.get("id").toString()));
        dto.setCode(map.get("code").toString());
        dto.setDeviceId(Long.parseLong(map.get("deviceId").toString()));
        dto.setStatus(Integer.parseInt(map.get("status").toString()));
        dto.setExpireTime(new Date(Long.parseLong(map.get("expireTime").toString())));
        dto.setCreateDate(new Date(Long.parseLong(map.get("createDate").toString())));
        return dto;
    }

    /**
     * 绑定设备到用户
     */
    private void bindDeviceToUser(Long deviceId, Long userId) {
        // 修改设备的userId字段
        deviceService.update(deviceId, userId);
    }

    /**
     * 将实体转换为DTO
     */
    private ActivationCodeDTO convertEntity(ActivationCodeEntity entity) {
        if (entity == null) {
            return null;
        }
        ActivationCodeDTO dto = new ActivationCodeDTO();
        org.springframework.beans.BeanUtils.copyProperties(entity, dto);
        return dto;
    }
} 