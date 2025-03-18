package xiaozhi.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * API Documentation Initializer
 * Ensures API documentation and Swagger-related configurations are correctly loaded at startup
 */
@Component
public class ApiDocInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ApiDocInitializer.class);
    
    @Value("${server.port:8002}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:/xiaozhi-esp32-api}")
    private String contextPath;
    
    private final OpenAPI openAPI;
    
    public ApiDocInitializer(OpenAPI openAPI) {
        this.openAPI = openAPI;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("API documentation initialized at http://localhost:{}{}{}",
                 serverPort, contextPath, "/doc.html");
        
        // Update OpenAPI title with context path for better clarity
        if (openAPI != null && openAPI.getInfo() != null) {
            Info info = openAPI.getInfo();
            info.setTitle("XiaoZhi ESP32 API");
            info.setDescription("Device Management System API Documentation - Context Path: " + contextPath);
        }
    }
} 