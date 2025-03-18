package xiaozhi.common.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Swagger Documentation Helper Controller - DISABLED for troubleshooting
 */
// @Controller // Disabled for troubleshooting
@RequestMapping("/api-doc")
public class SwaggerHelper {

    /**
     * Redirect to Swagger UI - DISABLED
     */
    @GetMapping(value = {"", "/"})
    public String index() {
        return "API documentation is disabled for troubleshooting";
    }

    /**
     * Redirect to Knife4j UI - DISABLED
     */
    @GetMapping("/knife4j")
    public String knife4j() {
        return "API documentation is disabled for troubleshooting";
    }

    /**
     * Redirect to API specification - DISABLED
     */
    @GetMapping("/json")
    public String apiDocs() {
        return "API documentation is disabled for troubleshooting";
    }

    /**
     * Redirect to API groups page - DISABLED
     */
    @GetMapping("/group")
    public String group() {
        return "API documentation is disabled for troubleshooting";
    }
    
    /**
     * Provide minimal valid OpenAPI JSON - DISABLED
     */
    @GetMapping(value = "/test-json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getTestJson() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("openapi", "3.0.1");
        doc.put("info", Map.of(
                "title", "API Disabled",
                "description", "API documentation is disabled for troubleshooting",
                "version", "1.0.0"
        ));
        doc.put("paths", new HashMap<>());
        return doc;
    }
} 