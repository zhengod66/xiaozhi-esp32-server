package xiaozhi.modules.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

import java.util.Date;

/**
 * 设备实体
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("t_device")
public class DeviceEntity extends BaseEntity {
    /**
     * 设备MAC地址
     */
    private String macAddress;

    /**
     * 设备UUID
     */
    private String clientId;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备类型
     */
    private String type;

    /**
     * 设备状态：0-未激活 1-等待激活 2-已激活
     */
    private Integer status;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 更新时间
     */
    private Date updateDate;
} 