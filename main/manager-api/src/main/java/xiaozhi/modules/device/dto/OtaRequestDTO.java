package xiaozhi.modules.device.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * OTA请求数据传输对象
 * 用于接收设备发送的请求参数
 */
@Data
public class OtaRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 设备MAC地址
     */
    private String macAddress;

    /**
     * 设备UUID
     */
    private String clientId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 访问令牌（已激活设备可能会有）
     */
    private String accessToken;
} 