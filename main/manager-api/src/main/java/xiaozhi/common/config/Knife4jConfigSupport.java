package xiaozhi.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Knife4j specific configuration support - DISABLED for troubleshooting
 */
// @Configuration // Disabled for troubleshooting
public class Knife4jConfigSupport implements WebMvcConfigurer {
    private static final Logger log = LoggerFactory.getLogger(Knife4jConfigSupport.class);
    
    @Value("${server.servlet.context-path:/xiaozhi-esp32-api}")
    private String contextPath;
    
    /**
     * Add resource handlers
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // All resource handlers disabled for troubleshooting
        log.info("Knife4j resource handlers disabled for troubleshooting");
    }
    
    /**
     * Knife4j request filter - DISABLED for troubleshooting
     */
    // @Component // Disabled for troubleshooting
    public static class Knife4jFilter extends OncePerRequestFilter {
        
        private static final Logger log = LoggerFactory.getLogger(Knife4jFilter.class);
        private static final Pattern API_DOCS_GROUP_PATTERN = Pattern.compile("/v3/api-docs/[^/]+$");
        
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
    }
} 