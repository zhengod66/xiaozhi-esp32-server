package xiaozhi.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check controller that doesn't require authentication
 * Used to test basic functionality without API documentation
 */
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    /**
     * Basic health check endpoint
     * @return Simple status message
     */
    @GetMapping
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Service is running");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
} 