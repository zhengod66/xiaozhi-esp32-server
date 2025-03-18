package xiaozhi.modules.security.oauth2;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.web.bind.annotation.RequestMethod;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.utils.HttpContextUtils;
import xiaozhi.common.utils.JsonUtils;
import xiaozhi.common.utils.Result;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 authentication filter
 */
public class Oauth2Filter extends AuthenticatingFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(Oauth2Filter.class);

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        // Get request token
        String token = getRequestToken((HttpServletRequest) request);

        if (StringUtils.isBlank(token)) {
            return null;
        }

        return new Oauth2Token(token);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        // Get request token, if token doesn't exist, return 401
        String token = getRequestToken((HttpServletRequest) request);
        if (StringUtils.isBlank(token)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setContentType("application/json;charset=utf-8");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());

            String json = JsonUtils.toJsonString(new Result().error(ErrorCode.UNAUTHORIZED));
            
            // Only log for non-documentation paths to reduce noise
            String path = ((HttpServletRequest) request).getRequestURI();
            if (!isDocumentationPath(path)) {
                logger.debug("Token is empty, returning 401 error for path: {}", path);
            }
            
            httpResponse.getWriter().print(json);

            return false;
        }

        return executeLogin(request, response);
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setContentType("application/json;charset=utf-8");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());
        try {
            // Handle login failure exception
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            Result r = new Result().error(ErrorCode.UNAUTHORIZED, throwable.getMessage());

            logger.debug("Login failed: {}", throwable.getMessage());

            String json = JsonUtils.toJsonString(r);
            httpResponse.getWriter().print(json);
        } catch (IOException e1) {
            logger.error("Error writing response", e1);
        }

        return false;
    }

    /**
     * Get token from request
     */
    private String getRequestToken(HttpServletRequest httpRequest) {
        String path = httpRequest.getRequestURI();
        
        // From header
        String token = httpRequest.getHeader(Constant.TOKEN_HEADER);
        
        // From Authorization header (Bearer token)
        if (StringUtils.isBlank(token)) {
            String authHeader = httpRequest.getHeader("Authorization");
            if (!StringUtils.isBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // From request parameter
        if (StringUtils.isBlank(token)) {
            token = httpRequest.getParameter(Constant.TOKEN_HEADER);
        }
        
        return token;
    }
    
    /**
     * Check if path is related to API documentation
     */
    private boolean isDocumentationPath(String path) {
        return path != null && (
            path.contains("/v3/api-docs") || 
            path.contains("/swagger-ui") || 
            path.contains("/doc.html") ||
            path.contains("/swagger-resources") ||
            path.contains("/api-doc") ||
            path.contains("/knife4j")
        );
    }
}