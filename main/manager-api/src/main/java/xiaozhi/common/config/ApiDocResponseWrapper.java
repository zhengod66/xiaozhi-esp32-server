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
 * API Documentation Response Wrapper
 * Handles proper formatting of API documentation responses
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiDocResponseWrapper extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(ApiDocResponseWrapper.class);
    private static final Pattern API_DOCS_PATTERN = Pattern.compile("(/v3/api-docs.*)|(/swagger-ui/.*)|(/doc\\.html.*)");
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Only filter API documentation related requests
        boolean isApiDocsRequest = path.contains("/v3/api-docs") || 
                                  path.contains("/swagger-ui") || 
                                  path.contains("/doc.html") ||
                                  path.contains("/swagger-resources");
        
        return !isApiDocsRequest;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        String path = request.getRequestURI();
        log.debug("ApiDocResponseWrapper processing path: {}", path);
        
        // Wrap the response to access its content
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        try {
            // 1. 在過濾器鏈執行前設置基本響應頭，確保正確的編碼
            if (path.contains("/v3/api-docs")) {
                // 預先設置正確的Content-Type和編碼
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                // 添加防止緩存的頭部，避免瀏覽器緩存可能錯誤的響應
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
            }
            
            // 2. 執行過濾器鏈
            filterChain.doFilter(request, responseWrapper);
            
            // 3. 處理API文檔響應
            if (path.contains("/v3/api-docs")) {
                log.debug("Processing API docs response for path: {}", path);
                String contentType = responseWrapper.getContentType();
                log.debug("Original Content-Type: {}", contentType);
                
                // 確保Content-Type正確設置
                if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                    log.debug("Setting Content-Type to: {}", MediaType.APPLICATION_JSON_VALUE);
                    responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
                }
                
                // 確保字符編碼正確設置
                responseWrapper.setCharacterEncoding(StandardCharsets.UTF_8.name());
                
                // 檢查響應內容
                byte[] content = responseWrapper.getContentAsByteArray();
                if (content.length > 0) {
                    String contentAsString = new String(content, StandardCharsets.UTF_8);
                    
                    // 記錄原始內容（有限長度）進行診斷
                    log.debug("API docs response content first 100 chars: {}", 
                              contentAsString.length() > 100 ? contentAsString.substring(0, 100) + "..." : contentAsString);
                    
                    // 處理內容被引號包裹的情況
                    if (contentAsString.startsWith("\"") && contentAsString.endsWith("\"")) {
                        log.warn("Response appears to be a quoted string. Unquoting...");
                        // 移除開頭和結尾的引號，並處理轉義字符
                        contentAsString = contentAsString.substring(1, contentAsString.length() - 1)
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\");
                    }
                    
                    // 檢測並處理Base64編碼的內容
                    if (contentAsString.startsWith("eyJ") && isBase64(contentAsString)) {
                        log.warn("Response appears to be Base64 encoded. Decoding...");
                        try {
                            String decoded = new String(java.util.Base64.getDecoder().decode(contentAsString), StandardCharsets.UTF_8);
                            log.debug("Base64 decoded content first 100 chars: {}", 
                                 decoded.length() > 100 ? decoded.substring(0, 100) + "..." : decoded);
                            contentAsString = decoded;
                        } catch (Exception e) {
                            log.warn("Failed to decode Base64 content: {}", e.getMessage());
                        }
                    }
                    
                    // 檢查內容是否為有效的JSON
                    try {
                        // 嘗試解析為JSON對象
                        Object jsonObj = objectMapper.readValue(contentAsString, Object.class);
                        log.debug("Response is valid JSON object");
                        
                        // 將內容寫回響應
                        responseWrapper.resetBuffer();
                        // 使用writeValue而不是writeValueAsString，避免額外的引號
                        objectMapper.writeValue(responseWrapper.getWriter(), jsonObj);
                        log.debug("Rewrote response as formatted JSON");
                    } catch (Exception e) {
                        log.warn("Content is not valid JSON: {}", e.getMessage());
                        
                        // 如果不是有效的JSON，返回一個基本的OpenAPI結構
                        Map<String, Object> fallbackResponse = new HashMap<>();
                        fallbackResponse.put("openapi", "3.0.1");
                        fallbackResponse.put("info", Map.of(
                                "title", "XiaoZhi ESP32 API",
                                "description", "API documentation - Response format error was fixed by the wrapper",
                                "version", "1.0.0"
                        ));
                        fallbackResponse.put("paths", new HashMap<>());
                        fallbackResponse.put("components", Map.of("schemas", new HashMap<>()));
                        fallbackResponse.put("original_error", "Response was not valid JSON: " + e.getMessage());
                        
                        responseWrapper.resetBuffer();
                        objectMapper.writeValue(responseWrapper.getWriter(), fallbackResponse);
                        log.warn("Used fallback OpenAPI JSON due to parsing error");
                    }
                } else {
                    log.warn("Empty API docs response for {}", path);
                    // 返回一個最小的有效OpenAPI定義
                    Map<String, Object> emptyResponse = new HashMap<>();
                    emptyResponse.put("openapi", "3.0.1");
                    emptyResponse.put("info", Map.of(
                            "title", "XiaoZhi ESP32 API",
                            "description", "API documentation - Empty response was detected",
                            "version", "1.0.0"
                    ));
                    emptyResponse.put("paths", new HashMap<>());
                    
                    responseWrapper.resetBuffer();
                    objectMapper.writeValue(responseWrapper.getWriter(), emptyResponse);
                    log.warn("Used empty OpenAPI JSON due to empty response");
                }
            }
        } finally {
            // 4. 確保響應內容被複製到原始響應
            responseWrapper.copyBodyToResponse();
        }
    }
    
    /**
     * 檢測字符串是否可能是Base64編碼
     */
    private boolean isBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        // 基本檢查：Base64字符集、長度是4的倍數（可能有填充）
        String base64Pattern = "^[A-Za-z0-9+/]*={0,2}$";
        return str.matches(base64Pattern) && (str.length() % 4 == 0 || str.length() % 4 == 2 || str.length() % 4 == 3);
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