package xiaozhi.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.common.service.impl.CrudServiceImpl;
import xiaozhi.modules.device.dao.DeviceDao;
import xiaozhi.modules.device.dto.DeviceDTO;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.device.service.DeviceService;

import java.util.Date;
import java.util.Map;

/**
 * 设备服务实现类
 */
@Service
@AllArgsConstructor
public class DeviceServiceImpl extends CrudServiceImpl<DeviceDao, DeviceEntity, DeviceDTO> implements DeviceService {

    /**
     * 根据MAC地址查询设备
     *
     * @param macAddress MAC地址
     * @return 设备信息
     */
    @Override
    public DeviceDTO getByMacAddress(String macAddress) {
        if (macAddress == null || macAddress.isEmpty()) {
            return null;
        }

        DeviceEntity entity = baseDao.selectOne(
                new LambdaQueryWrapper<DeviceEntity>()
                        .eq(DeviceEntity::getMacAddress, macAddress)
        );

        return entity != null ? convertEntity(entity) : null;
    }

    /**
     * 根据ClientID查询设备
     *
     * @param clientId 客户端ID
     * @return 设备信息
     */
    @Override
    public DeviceDTO getByClientId(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            return null;
        }

        DeviceEntity entity = baseDao.selectOne(
                new LambdaQueryWrapper<DeviceEntity>()
                        .eq(DeviceEntity::getClientId, clientId)
        );

        return entity != null ? convertEntity(entity) : null;
    }

    /**
     * 更新设备状态
     *
     * @param id     设备ID
     * @param status 新状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return;
        }

        DeviceEntity entity = new DeviceEntity();
        entity.setId(id);
        entity.setStatus(status);
        entity.setUpdateDate(new Date());

        baseDao.updateById(entity);
    }

    /**
     * 绑定设备到用户
     *
     * @param id     设备ID
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, Long userId) {
        baseDao.update(null, new LambdaUpdateWrapper<DeviceEntity>()
                .eq(DeviceEntity::getId, id)
                .set(DeviceEntity::getUserId, userId)
                .set(DeviceEntity::getUpdateDate, new Date()));
    }

    @Override
    public com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DeviceEntity> getWrapper(Map<String, Object> params) {
        // 创建查询包装器
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DeviceEntity> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        // 根据参数添加查询条件
        if (params != null) {
            if (params.containsKey("macAddress")) {
                wrapper.eq("mac_address", params.get("macAddress"));
            }
            
            if (params.containsKey("status")) {
                wrapper.eq("status", params.get("status"));
            }
        }
        
        // 添加默认排序
        wrapper.orderByDesc("create_date");
        
        return wrapper;
    }
    
    /**
     * 将实体转换为DTO
     */
    private DeviceDTO convertEntity(DeviceEntity entity) {
        if (entity == null) {
            return null;
        }
        DeviceDTO dto = new DeviceDTO();
        org.springframework.beans.BeanUtils.copyProperties(entity, dto);
        return dto;
    }
} 