package xiaozhi.modules.device.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OTA配置自動加載類
 * 從application.yml加載WebSocket和OTA相關配置
 */
@Configuration
public class OtaConfigAutoConfiguration {

    /**
     * 創建OTA配置實例
     * 
     * @param properties 從配置文件加載的屬性
     * @return OTA配置實例
     */
    @Bean
    public OtaConfig otaConfig(OtaProperties properties) {
        OtaConfig config = new OtaConfig();
        config.setServer(properties.getServer());
        config.setPort(properties.getPort());
        config.setPath(properties.getPath());
        config.setOtaPath(properties.getOtaPath());
        config.setEnableDeviceTypeMapping(properties.isEnableDeviceTypeMapping());
        return config;
    }
} 