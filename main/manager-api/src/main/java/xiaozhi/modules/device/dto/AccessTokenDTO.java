package xiaozhi.modules.device.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 访问令牌DTO
 */
@Data
public class AccessTokenDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * 关联设备信息
     */
    private DeviceDTO device;

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

    /**
     * 创建时间
     */
    private Date createDate;
} 