package xiaozhi.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger Configuration
 * Note: Group names must be in English to avoid URL encoding issues
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * Device Management API Group
     * Contains device-related operations like OTA, device management, activation codes and access tokens
     */
    @Bean
    public GroupedOpenApi deviceApi() {
        return GroupedOpenApi.builder()
                .group("device")
                .displayName("設備管理")
                .pathsToMatch(
                    "/xiaozhi/ota/**",      // OTA相關接口
                    "/ota/**",              // OTA相關接口的替代路徑
                    "/device/**",           // 設備基本管理
                    "/api/v1/activation/**", // 激活碼管理
                    "/token/**"              // 訪問令牌管理
                )
                .pathsToExclude(getExcludedPaths()) // 排除調試和臨時接口
                .build();
    }

    /**
     * System Management API Group
     * Contains system administration operations like user management
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .displayName("系統管理")
                .pathsToMatch(
                    "/sys/**",              // 系統管理接口
                    "/captcha",             // 驗證碼
                    "/login",               // 登錄
                    "/admin/**"             // 管理員接口
                )
                .pathsToExclude(getExcludedPaths()) // 排除調試和臨時接口
                .build();
    }
    
    /**
     * Health and Diagnostics API Group
     * Contains health checks and system diagnostics endpoints
     */
    @Bean
    public GroupedOpenApi healthApi() {
        return GroupedOpenApi.builder()
                .group("health")
                .displayName("健康檢查")
                .pathsToMatch(
                    "/health/**"            // 健康檢查接口
                )
                .pathsToExclude(getExcludedPaths()) // 排除調試和臨時接口
                .build();
    }
    
    /**
     * Excluded Paths
     * These paths are explicitly excluded from all API documentation groups
     * (debug endpoints, temporary APIs, etc.)
     */
    private String[] getExcludedPaths() {
        return new String[] {
            "/api/debug/**",        // 調試端點，僅用於開發測試
            "/api/test/**",         // 測試端點，僅用於開發測試
            "/api-doc/**",          // API文檔輔助接口，非業務接口
            "/knife4j/**",          // Knife4j UI相關接口，非業務接口
            "/swagger-resources/**", // Swagger資源
            "/swagger-ui/**",       // Swagger UI
            "/favicon.ico",         // 網站圖標
            "/error"                // 錯誤頁面
        };
    }

    /**
     * Minimal OpenAPI configuration to ensure standard format
     * Set basic information and server URL
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("小智 ESP32 設備管理系統 API")
                .description("ESP32設備管理系統API文檔 - 系統支持設備OTA、激活碼管理、設備管理等功能")
                .version("v1.0.0"))
            .servers(List.of(
                new Server().url("/xiaozhi-esp32-api")
            ));
    }
}