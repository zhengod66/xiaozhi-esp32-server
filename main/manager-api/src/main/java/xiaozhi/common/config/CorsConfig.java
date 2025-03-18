package xiaozhi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * 允許API文檔相關請求跨域訪問
 */
@Configuration
public class CorsConfig {
    
    /**
     * 配置CORS過濾器
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 創建CORS配置
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        // 配置API文檔相關路徑的跨域支持
        source.registerCorsConfiguration("/v3/api-docs/**", config);
        source.registerCorsConfiguration("/swagger-ui/**", config);
        source.registerCorsConfiguration("/knife4j/**", config);
        source.registerCorsConfiguration("/api-doc/**", config);
        source.registerCorsConfiguration("/doc.html", config);
        source.registerCorsConfiguration("/swagger-resources/**", config);
        source.registerCorsConfiguration("/webjars/**", config);
        source.registerCorsConfiguration("/api/test/**", config);
        
        return new CorsFilter(source);
    }
} 