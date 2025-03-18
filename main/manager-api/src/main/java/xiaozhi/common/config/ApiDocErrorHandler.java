package xiaozhi.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;

/**
 * API Documentation Error Handler - DISABLED for troubleshooting
 */
@Slf4j
// @Configuration // Disabled for troubleshooting
public class ApiDocErrorHandler implements WebMvcConfigurer {
    
    @Value("${server.servlet.context-path:/xiaozhi-esp32-api}")
    private String contextPath;
    
    @Value("${knife4j.enable:false}")
    private boolean knife4jEnabled;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("API Documentation handler completely disabled for troubleshooting");
        // All interceptors disabled for troubleshooting
    }
    
    /**
     * API Documentation Request Interceptor
     */
    private class ApiDocInterceptor implements HandlerInterceptor {
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // Get request path
            String requestURI = request.getRequestURI();
            
            // Set correct Content-Type to ensure JSON is returned
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            
            // If it's an API group request and contains URL encoding, try to redirect to the correct English group
            if (requestURI.contains("/v3/api-docs/") && !requestURI.endsWith("/swagger-config")) {
                try {
                    // Get group name (last part of URL)
                    String[] pathParts = requestURI.split("/");
                    String groupName = pathParts[pathParts.length - 1];
                    
                    // URL decoding
                    groupName = URLDecoder.decode(groupName, StandardCharsets.UTF_8.name());
                    
                    // Check group name, if it's Chinese, redirect to the corresponding English group
                    if ("System Management".equals(groupName)) {
                        log.info("Detected System Management group request, redirecting to system group");
                        response.sendRedirect(contextPath + "/v3/api-docs/system");
                        return false;
                    } else if ("Device Management".equals(groupName)) {
                        log.info("Detected Device Management group request, redirecting to device group");
                        response.sendRedirect(contextPath + "/v3/api-docs/device");
                        return false;
                    }
                    
                    log.info("Processing API doc group request: {}, decoded: {}", requestURI, groupName);
                } catch (Exception e) {
                    log.error("Error processing API doc group request: {}", e.getMessage(), e);
                }
            }
            
            return true;
        }
        
        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
            // No additional processing needed
        }
        
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            // If an exception occurs, return the minimum valid JSON
            if (ex != null) {
                log.error("Error processing API documentation request: {}, URI: {}", ex.getMessage(), request.getRequestURI(), ex);
                try {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    
                    // Return minimum valid OpenAPI JSON
                    String fallbackJson = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"XiaoZhi ESP32 API\",\"version\":\"1.0.0\"},\"paths\":{}}";
                    response.getWriter().write(fallbackJson);
                    response.getWriter().flush();
                } catch (IOException e) {
                    log.error("Unable to write error response", e);
                }
            }
        }
    }
} 