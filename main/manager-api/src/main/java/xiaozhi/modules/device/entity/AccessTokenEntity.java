package xiaozhi.modules.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

import java.util.Date;

/**
 * 访问令牌实体
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("t_access_token")
public class AccessTokenEntity extends BaseEntity {
    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 是否已撤销：0-否 1-是
     */
    private Integer isRevoked;

    /**
     * 过期时间
     */
    private Date expireTime;
} 