package xiaozhi.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * API文檔測試控制器
 * 提供簡單的端點來測試Swagger/Knife4j是否正常工作
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "API測試", description = "用於測試API文檔系統是否正常工作")
public class ApiDocsTestController {
    
    @Value("${server.port:8002}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:/xiaozhi-esp32-api}")
    private String contextPath;
    
    /**
     * 測試端點，返回基本信息
     */
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "獲取基本信息", description = "返回服務器基本配置信息")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "ok");
        info.put("message", "API文檔測試成功");
        info.put("server", "小智ESP32 API服務器");
        info.put("port", serverPort);
        info.put("contextPath", contextPath);
        
        // API文檔訪問URL
        Map<String, String> docUrls = new HashMap<>();
        String baseUrl = "http://localhost:" + serverPort + contextPath;
        docUrls.put("swaggerUi", baseUrl + "/swagger-ui/index.html");
        docUrls.put("knife4j", baseUrl + "/doc.html");
        docUrls.put("apiDocs", baseUrl + "/v3/api-docs");
        docUrls.put("systemGroup", baseUrl + "/v3/api-docs/system");
        docUrls.put("deviceGroup", baseUrl + "/v3/api-docs/device");
        docUrls.put("apiDocsUi", baseUrl + "/api-doc");
        
        info.put("docUrls", docUrls);
        info.put("timestamp", System.currentTimeMillis());
        
        return info;
    }
    
    /**
     * 最小化API測試
     */
    @GetMapping("/ping")
    @Operation(summary = "Ping測試", description = "最簡單的API測試")
    public Map<String, String> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "pong");
        return response;
    }
    
    /**
     * OpenAPI規範測試
     */
    @GetMapping("/openapi")
    @Operation(summary = "OpenAPI規範測試", description = "測試OpenAPI規範是否可訪問")
    public Map<String, Object> testOpenApi() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("version", "3.0.1");
        result.put("title", "小智ESP32 API");
        result.put("paths", new HashMap<>());
        return result;
    }
    
    /**
     * 測試特定分組的API文檔
     */
    @GetMapping("/group/{groupName}")
    @Operation(summary = "測試API分組", description = "測試特定分組的API文檔是否可訪問")
    public Map<String, Object> testGroup(@PathVariable String groupName) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("group", groupName);
        result.put("url", contextPath + "/v3/api-docs/" + groupName);
        
        try {
            // 構建基本的OpenAPI結構
            Map<String, Object> openapi = new HashMap<>();
            openapi.put("openapi", "3.0.1");
            
            Map<String, String> info = new HashMap<>();
            info.put("title", "分組 " + groupName + " 的API文檔");
            info.put("version", "1.0.0");
            openapi.put("info", info);
            openapi.put("paths", new HashMap<>());
            
            result.put("schema", openapi);
            result.put("message", "可以通過 " + contextPath + "/v3/api-docs/" + groupName + " 訪問該分組的API文檔");
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return result;
    }
} 