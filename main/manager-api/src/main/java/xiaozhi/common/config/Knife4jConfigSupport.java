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
 * Knife4j specific configuration support
 */
@Configuration
public class Knife4jConfigSupport implements WebMvcConfigurer {
    private static final Logger log = LoggerFactory.getLogger(Knife4jConfigSupport.class);
    
    @Value("${server.servlet.context-path:/xiaozhi-esp32-api}")
    private String contextPath;
    
    /**
     * Add resource handlers
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("Adding Knife4j resource handlers");
        
        // Add resources for Knife4j
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        // Optional: Add additional resources if needed
        registry.addResourceHandler("/swagger-resources/**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-resources/");
    }
    
    /**
     * Knife4j request filter
     */
    @Component
    public static class Knife4jFilter extends OncePerRequestFilter {
        
        private static final Logger log = LoggerFactory.getLogger(Knife4jFilter.class);
        private static final Pattern API_DOCS_GROUP_PATTERN = Pattern.compile("/v3/api-docs/[^/]+$");
        
        @Autowired
        private ObjectMapper objectMapper;
        
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();
            boolean isApiDocsRequest = path.contains("/v3/api-docs") || path.contains("/swagger-ui") || 
                                       path.contains("/doc.html") || path.contains("/knife4j");
            
            return !isApiDocsRequest; // Only filter API docs requests
        }
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
                throws ServletException, IOException {
            String path = request.getRequestURI();
            
            // Handle API docs group requests
            if (API_DOCS_GROUP_PATTERN.matcher(path).matches()) {
                log.debug("Processing API docs group request: {}", path);
                
                // Just enhance the response headers
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            }
            
            // Continue the filter chain
            filterChain.doFilter(request, response);
        }
    }
} 