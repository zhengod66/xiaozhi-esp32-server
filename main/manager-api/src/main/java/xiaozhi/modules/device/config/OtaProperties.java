package xiaozhi.modules.device.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OTA属性类
 * 从配置文件加载属性
 */
@Data
@Component("otaProperties")
@ConfigurationProperties(prefix = "xiaozhi.ws")
public class OtaProperties {
    /**
     * WebSocket服务器地址
     */
    private String server = "localhost";

    /**
     * WebSocket服务端口
     */
    private int port = 8765;

    /**
     * WebSocket路径
     */
    private String path = "/ws";

    /**
     * OTA接口路径
     */
    private String otaPath = "/xiaozhi/ota";

    /**
     * 是否启用设备类型映射
     */
    private boolean enableDeviceTypeMapping = false;
} 