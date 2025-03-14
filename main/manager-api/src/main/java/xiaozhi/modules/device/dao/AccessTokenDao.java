package xiaozhi.modules.device.dao;

import org.apache.ibatis.annotations.Mapper;
import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.device.entity.AccessTokenEntity;

/**
 * 访问令牌DAO接口
 */
@Mapper
public interface AccessTokenDao extends BaseDao<AccessTokenEntity> {
    
} 