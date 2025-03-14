package xiaozhi.modules.device.dao;

import org.apache.ibatis.annotations.Mapper;
import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.device.entity.ActivationCodeEntity;

/**
 * 激活码DAO接口
 */
@Mapper
public interface ActivationCodeDao extends BaseDao<ActivationCodeEntity> {
    
} 