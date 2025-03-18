package xiaozhi.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;

import java.util.HashMap;
import java.util.Map;

/**
 * API Documentation Debug Controller - DISABLED for troubleshooting
 */
@Slf4j
// @Controller // Disabled for troubleshooting
@RequestMapping("/api/debug")
public class ApiDocDebugController {

    // Constructor arguments injections remain to avoid changing too much code
    // but the controller is disabled via @Controller annotation
    
    @Value("${server.servlet.context-path:/xiaozhi-esp32-api}")
    private String contextPath;
    
    private GroupedOpenApi deviceApi;
    private GroupedOpenApi systemApi;
    
    /**
     * Constructor remains but controller is inactive
     */
    public ApiDocDebugController(
            // @Autowired - commented out since we're disabling the controller
            OpenApiWebMvcResource openApiResource,
            // @Autowired - commented out since we're disabling the controller  
            GroupedOpenApi deviceApi,
            // @Autowired - commented out since we're disabling the controller
            GroupedOpenApi systemApi
    ) {
        // Controller is disabled
    }

    @GetMapping(value = "/api-docs/{groupName}", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getApiDocsForGroup(@PathVariable String groupName) {
        // Controller disabled - this would never be called
        return "API documentation is disabled for troubleshooting";
    }

    @GetMapping(value = "/api-docs-fixed/{groupName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFixedApiDocs(@PathVariable String groupName) {
        // Controller disabled - this would never be called
        Map<String, Object> response = new HashMap<>();
        response.put("openapi", "3.0.1");
        response.put("info", Map.of(
                "title", "API Disabled",
                "description", "API documentation is disabled for troubleshooting",
                "version", "1.0.0"
        ));
        response.put("paths", new HashMap<>());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/knife4j")
    public String getFixedKnife4j() {
        // Controller disabled - this would never be called
        return "API documentation is disabled for troubleshooting";
    }
} 