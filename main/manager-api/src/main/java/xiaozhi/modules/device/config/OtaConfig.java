package xiaozhi.modules.device.config;

import org.springframework.context.annotation.Configuration;

/**
 * OTA配置類
 * 用於保存WebSocket服務器的配置信息
 */
@Configuration
public class OtaConfig {
    
    /**
     * WebSocket服務器地址
     */
    private String server = "localhost";
    
    /**
     * WebSocket服務器端口
     */
    private Integer port = 8765;
    
    /**
     * WebSocket路徑
     */
    private String path = "/ws";
    
    /**
     * OTA接口路徑
     */
    private String otaPath = "/xiaozhi/ota";
    
    /**
     * 設備類型映射（預留功能，未來可能根據設備類型返回不同的配置）
     */
    private boolean enableDeviceTypeMapping = false;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOtaPath() {
        return otaPath;
    }

    public void setOtaPath(String otaPath) {
        this.otaPath = otaPath;
    }

    public boolean isEnableDeviceTypeMapping() {
        return enableDeviceTypeMapping;
    }

    public void setEnableDeviceTypeMapping(boolean enableDeviceTypeMapping) {
        this.enableDeviceTypeMapping = enableDeviceTypeMapping;
    }
} 