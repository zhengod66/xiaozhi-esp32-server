package xiaozhi.modules.device.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 激活码DTO
 */
@Data
public class ActivationCodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 6位数字激活码
     */
    private String code;

    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * 关联设备信息
     */
    private DeviceDTO device;

    /**
     * 状态：0-有效 1-已使用 2-已过期
     */
    private Integer status;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createDate;
} 