package xiaozhi.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * API Documentation Initializer - DISABLED for troubleshooting
 * Ensures API documentation and Swagger-related configurations are correctly loaded at startup
 */
// @Component // Disabled for troubleshooting
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
        log.info("API documentation initialization disabled for troubleshooting");
        
        // No initialization for now - disabled for troubleshooting
    }
} 