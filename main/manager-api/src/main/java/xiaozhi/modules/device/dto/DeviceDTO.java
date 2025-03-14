package xiaozhi.modules.device.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 设备DTO
 */
@Data
public class DeviceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

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
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新时间
     */
    private Date updateTime;
} 