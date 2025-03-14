package xiaozhi.modules.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

import java.util.Date;

/**
 * 激活码实体
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("t_activation_code")
public class ActivationCodeEntity extends BaseEntity {
    /**
     * 6位数字激活码
     */
    private String code;

    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * 状态：0-有效 1-已使用 2-已过期
     */
    private Integer status;

    /**
     * 过期时间
     */
    private Date expireTime;
} 