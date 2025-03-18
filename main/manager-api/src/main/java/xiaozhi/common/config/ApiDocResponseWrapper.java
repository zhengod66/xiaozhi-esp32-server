package xiaozhi.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * API Documentation Response Wrapper - DISABLED for troubleshooting
 */
// @Component // Disabled for troubleshooting
// @Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiDocResponseWrapper extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(ApiDocResponseWrapper.class);
    private static final Pattern API_DOCS_PATTERN = Pattern.compile("(/v3/api-docs.*)|(/swagger-ui/.*)|(/doc\\.html.*)");
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Always return true - disabled for troubleshooting
        return true;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        // Simple pass-through implementation
        filterChain.doFilter(request, response);
    }
    
    /**
     * Create master API documentation JSON
     */
    private String createMasterOpenApiJson() throws IOException {
        Map<String, Object> openapi = new HashMap<>();
        openapi.put("openapi", "3.0.1");
        
        Map<String, String> info = new HashMap<>();
        info.put("title", "XiaoZhi ESP32 API");
        info.put("description", "Device Management System API Documentation - Auto-generated");
        info.put("version", "1.0.0");
        openapi.put("info", info);
        
        // Add groups
        Map<String, Object> paths = new HashMap<>();
        openapi.put("paths", paths);
        
        Map<String, Object> components = new HashMap<>();
        openapi.put("components", components);
        
        // Add tags
        openapi.put("tags", java.util.List.of(
                Map.of("name", "device", "description", "Device Management"),
                Map.of("name", "system", "description", "System Management")
        ));
        
        return objectMapper.writeValueAsString(openapi);
    }
    
    /**
     * Create group-specific OpenAPI JSON
     */
    private String createGroupOpenApiJson(String groupName) throws IOException {
        Map<String, Object> openapi = new HashMap<>();
        openapi.put("openapi", "3.0.1");
        
        Map<String, String> info = new HashMap<>();
        String title;
        String description;
        
        // Set title and description based on group name
        if ("device".equals(groupName)) {
            title = "Device Management API";
            description = "APIs related to device management";
        } else if ("system".equals(groupName)) {
            title = "System Management API";
            description = "APIs related to system management";
        } else {
            title = groupName + " API";
            description = "APIs related to " + groupName;
        }
        
        info.put("title", title);
        info.put("description", description);
        info.put("version", "1.0.0");
        openapi.put("info", info);
        
        // Add empty paths and components
        openapi.put("paths", new HashMap<>());
        openapi.put("components", Map.of("schemas", new HashMap<>()));
        
        return objectMapper.writeValueAsString(openapi);
    }
    
    /**
     * Create default OpenAPI JSON
     */
    private String createDefaultOpenApiJson() throws IOException {
        Map<String, Object> openapi = new HashMap<>();
        openapi.put("openapi", "3.0.1");
        
        Map<String, String> info = new HashMap<>();
        info.put("title", "XiaoZhi ESP32 API");
        info.put("description", "This is default API documentation content");
        info.put("version", "1.0.0");
        openapi.put("info", info);
        
        openapi.put("paths", new HashMap<>());
        openapi.put("components", Map.of("schemas", new HashMap<>()));
        
        return objectMapper.writeValueAsString(openapi);
    }
} 