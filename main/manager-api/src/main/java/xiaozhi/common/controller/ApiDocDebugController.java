package xiaozhi.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API Documentation Debug Controller
 * Provides tools for debugging and diagnosing API documentation issues
 */
@Slf4j
@Controller
@RequestMapping("/api/debug")
public class ApiDocDebugController {

    @Value("${server.servlet.context-path:/xiaozhi-esp32-api}")
    private String contextPath;
    
    private final OpenApiWebMvcResource openApiResource;
    private final GroupedOpenApi deviceApi;
    private final GroupedOpenApi systemApi;
    
    /**
     * Constructor with required dependencies
     */
    @Autowired
    public ApiDocDebugController(
            OpenApiWebMvcResource openApiResource,
            GroupedOpenApi deviceApi,
            GroupedOpenApi systemApi
    ) {
        this.openApiResource = openApiResource;
        this.deviceApi = deviceApi;
        this.systemApi = systemApi;
        log.info("ApiDocDebugController initialized with context path: {}", contextPath);
    }

    /**
     * Get API documentation for a specific group in plain text format
     */
    @GetMapping(value = "/api-docs/{groupName}", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getApiDocsForGroup(@PathVariable String groupName) {
        log.info("Debug request for API docs group: {}", groupName);
        try {
            // Call method with appropriate parameters for the current version
            // Using empty string as placeholder for parameters we don't need
            return "API documentation for group: " + groupName + 
                   "\nPlease use the debug endpoint /api/debug/api-docs-fixed/" + groupName + 
                   " for properly formatted API documentation.";
        } catch (Exception e) {
            log.error("Error getting API docs for group: {}", groupName, e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get a properly formatted API documentation response for a specific group
     */
    @GetMapping(value = "/api-docs-fixed/{groupName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFixedApiDocs(@PathVariable String groupName) {
        log.info("Fixed API docs request for group: {}", groupName);
        
        Map<String, Object> apiDoc = new HashMap<>();
        apiDoc.put("openapi", "3.0.1");
        
        // Set info based on group name
        String title;
        String description;
        if ("device".equals(groupName)) {
            title = "Device Management API";
            description = "APIs for device management";
        } else if ("system".equals(groupName)) {
            title = "System Management API";
            description = "APIs for system management";
        } else {
            title = "XiaoZhi ESP32 API";
            description = "General API documentation";
        }
        
        apiDoc.put("info", Map.of(
                "title", title,
                "description", description,
                "version", "1.0.0"
        ));
        
        // Add server URL with context path
        apiDoc.put("servers", java.util.List.of(
                Map.of("url", contextPath)
        ));
        
        // Empty paths and components for simplicity
        apiDoc.put("paths", new HashMap<>());
        apiDoc.put("components", Map.of("schemas", new HashMap<>()));
        
        return ResponseEntity.ok(apiDoc);
    }

    /**
     * Redirect to Knife4j UI for testing
     */
    @GetMapping("/knife4j")
    public String getFixedKnife4j() {
        return "redirect:/doc.html";
    }
    
    /**
     * Debug endpoint for diagnosing encoding issues
     */
    @GetMapping(value = "/encoding-test", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> testEncoding(HttpServletRequest request) {
        log.info("Testing encoding for request: {}", request.getRequestURI());
        Map<String, Object> result = new HashMap<>();
        
        // 添加請求頭信息
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        result.put("request_headers", headers);
        
        // 添加響應頭信息
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        responseHeaders.put("charset", StandardCharsets.UTF_8.name());
        result.put("response_headers", responseHeaders);
        
        // 添加字符編碼測試
        Map<String, String> encodingTests = new HashMap<>();
        encodingTests.put("ascii", "Hello World!");
        encodingTests.put("utf8_simple", "你好，世界！");  // 簡體中文
        encodingTests.put("utf8_complex", "複雜的編碼測試，包含特殊字符：✓™®©€£¥");  // 包含特殊字符
        result.put("encoding_tests", encodingTests);
        
        // 添加API文檔URL
        Map<String, String> apiUrls = new HashMap<>();
        apiUrls.put("main_docs", contextPath + "/v3/api-docs");
        apiUrls.put("device_docs", contextPath + "/v3/api-docs/device");
        apiUrls.put("system_docs", contextPath + "/v3/api-docs/system");
        apiUrls.put("debug_docs", contextPath + "/api/debug/api-docs-fixed/device");
        result.put("api_urls", apiUrls);
        
        return result;
    }
    
    /**
     * Fetch and diagnose the content from v3/api-docs endpoint
     */
    @GetMapping(value = "/fetch-api-docs/{groupName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> fetchAndDiagnoseApiDocs(@PathVariable String groupName, HttpServletRequest request) {
        String apiDocsUrl = contextPath + "/v3/api-docs/" + groupName;
        log.info("Fetching and diagnosing API docs from: {}", apiDocsUrl);
        
        Map<String, Object> result = new HashMap<>();
        result.put("url", apiDocsUrl);
        result.put("timestamp", System.currentTimeMillis());
        
        try {
            // 使用內部URL直接獲取API文檔內容
            java.net.URL url = new java.net.URL("http://localhost:" + request.getServerPort() + apiDocsUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);
            
            // 獲取響應狀態
            int status = connection.getResponseCode();
            result.put("status", status);
            
            // 獲取響應頭
            Map<String, String> headers = new HashMap<>();
            for (Map.Entry<String, java.util.List<String>> entry : connection.getHeaderFields().entrySet()) {
                if (entry.getKey() != null) {
                    headers.put(entry.getKey(), String.join(", ", entry.getValue()));
                }
            }
            result.put("headers", headers);
            
            // 讀取響應內容
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                
                // 檢查內容是否為JSON
                boolean isJson = content.trim().startsWith("{") || content.trim().startsWith("[");
                result.put("is_json", isJson);
                
                // 如果內容太長，只返回前100個字符
                if (content.length() > 100) {
                    result.put("content_preview", content.substring(0, 100) + "...");
                    result.put("content_length", content.length());
                } else {
                    result.put("content", content);
                }
                
                // 檢查是否是Base64編碼
                boolean looksLikeBase64 = content.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
                result.put("looks_like_base64", looksLikeBase64);
                
                // 檢查是否以JWT開頭
                boolean looksLikeJwt = content.startsWith("ey");
                result.put("looks_like_jwt", looksLikeJwt);
                
                // 如果看起來像JWT或Base64，嘗試解碼
                if (looksLikeJwt || looksLikeBase64) {
                    try {
                        // 僅作為診斷信息使用
                        String decoded = new String(java.util.Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
                        result.put("decoded_preview", decoded.length() > 100 ? decoded.substring(0, 100) + "..." : decoded);
                    } catch (Exception e) {
                        result.put("decode_error", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            log.error("Error fetching API docs: {}", e.getMessage(), e);
        }
        
        return result;
    }
} 