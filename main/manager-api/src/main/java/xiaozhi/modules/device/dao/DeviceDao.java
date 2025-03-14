package xiaozhi.modules.device.dao;

import org.apache.ibatis.annotations.Mapper;
import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.device.entity.DeviceEntity;

/**
 * 设备DAO接口
 */
@Mapper
public interface DeviceDao extends BaseDao<DeviceEntity> {
    
} 