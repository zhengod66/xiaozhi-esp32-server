package xiaozhi.common.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Swagger Documentation Helper Controller
 */
@Controller
@RequestMapping("/api-doc")
public class SwaggerHelper {

    /**
     * Redirect to Swagger UI
     */
    @GetMapping(value = {"", "/"})
    public String index() {
        return "redirect:/swagger-ui/index.html";
    }

    /**
     * Redirect to Knife4j UI
     */
    @GetMapping("/knife4j")
    public String knife4j() {
        return "redirect:/doc.html";
    }

    /**
     * Redirect to API specification
     */
    @GetMapping("/json")
    public String apiDocs() {
        return "redirect:/v3/api-docs";
    }

    /**
     * Redirect to API groups page
     */
    @GetMapping("/group")
    public String group() {
        return "redirect:/swagger-ui/index.html";
    }
    
    /**
     * Provide minimal valid OpenAPI JSON
     */
    @GetMapping(value = "/test-json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getTestJson() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("openapi", "3.0.1");
        doc.put("info", Map.of(
                "title", "XiaoZhi ESP32 API",
                "description", "API documentation test endpoint",
                "version", "1.0.0"
        ));
        doc.put("paths", new HashMap<>());
        return doc;
    }
} 