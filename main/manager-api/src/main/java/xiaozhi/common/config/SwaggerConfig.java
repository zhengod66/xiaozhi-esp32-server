package xiaozhi.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger Configuration - DISABLED for troubleshooting
 * Note: Group names must be in English to avoid URL encoding issues
 */
// @Configuration  // Disabled for troubleshooting
public class SwaggerConfig {
    
    /**
     * Device Management API Group
     * Note: 'group' must be in English (e.g. "device"), 'displayName' can be changed later
     */
    // @Bean  // Disabled for troubleshooting
    public GroupedOpenApi deviceApi() {
        return GroupedOpenApi.builder()
                .group("device")
                .displayName("Device Management")
                .pathsToMatch("/ota/**", "/api/device/**", "/xiaozhi/ota/**")
                .build();
    }

    /**
     * System Management API Group
     * Note: 'group' must be in English (e.g. "system"), 'displayName' can be changed later
     */
    // @Bean  // Disabled for troubleshooting
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .displayName("System Management")
                .pathsToMatch("/api/sys/**")
                .build();
    }

    /**
     * Minimal OpenAPI configuration to ensure standard format
     * Set basic information and server URL
     * 
     * Note: No security scheme configuration to ensure documentation is accessible without authentication
     */
    // @Bean  // Disabled for troubleshooting
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("XiaoZhi ESP32 API")
                .description("Device Management System API Documentation - Using English for compatibility")
                .version("v1.0.0"))
            .servers(List.of(
                new Server().url("/xiaozhi-esp32-api")
            ));
    }
}