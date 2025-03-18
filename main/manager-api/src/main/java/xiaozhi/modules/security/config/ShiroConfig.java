package xiaozhi.modules.security.config;

import xiaozhi.modules.security.oauth2.Oauth2Filter;
import xiaozhi.modules.security.oauth2.Oauth2Realm;
import jakarta.servlet.Filter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.config.ShiroFilterConfiguration;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro的配置文件
 * Copyright (c) 人人开源 All rights reserved.
 * Website: https://www.renren.io
 */
@Configuration
public class ShiroConfig {

    @Bean
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionValidationSchedulerEnabled(false);
        sessionManager.setSessionIdUrlRewritingEnabled(false);

        return sessionManager;
    }

    @Bean("securityManager")
    public SecurityManager securityManager(Oauth2Realm oAuth2Realm, SessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(oAuth2Realm);
        securityManager.setSessionManager(sessionManager);
        securityManager.setRememberMeManager(null);
        return securityManager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        ShiroFilterConfiguration config = new ShiroFilterConfiguration();
        config.setFilterOncePerRequest(true);

        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);
        shiroFilter.setShiroFilterConfiguration(config);

        //oauth過濾
        Map<String, Filter> filters = new HashMap<>();
        filters.put("oauth2", new Oauth2Filter());
        shiroFilter.setFilters(filters);

        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/webjars/**", "anon");
        filterMap.put("/druid/**", "anon");
        filterMap.put("/login", "anon");
        filterMap.put("/publicKey", "anon");
        filterMap.put("/health", "anon");
        
        // API文檔調試相關路徑 - 添加調試端點
        filterMap.put("/api/debug/**", "anon");  // 添加我們的調試端點
        
        // Swagger和API文檔相關路徑
        filterMap.put("/v3/api-docs", "anon");
        filterMap.put("/v3/api-docs/**", "anon");
        filterMap.put("/swagger-config", "anon");
        filterMap.put("/swagger-ui.html", "anon");
        filterMap.put("/swagger-ui/**", "anon");
        filterMap.put("/swagger-resources", "anon");
        filterMap.put("/swagger-resources/**", "anon");
        filterMap.put("/doc.html", "anon");
        filterMap.put("/doc.html/**", "anon");
        filterMap.put("/webjars/**", "anon");
        filterMap.put("/knife4j/**", "anon");
        filterMap.put("/api-doc", "anon");
        filterMap.put("/api-doc/**", "anon");
        
        // 額外添加可能被遺漏的路徑
        filterMap.put("/configuration/ui", "anon");
        filterMap.put("/configuration/security", "anon");
        filterMap.put("/v2/api-docs", "anon");
        filterMap.put("/v2/api-docs/**", "anon");
        filterMap.put("/csrf", "anon");
        filterMap.put("/favicon.ico", "anon");
        
        filterMap.put("/sys/oss/download/**", "anon");
        filterMap.put("/captcha", "anon");
        filterMap.put("/mobile/**", "anon");
        
        // 允許設備管理相關公共接口
        filterMap.put("/admin/token/clean", "anon"); // 允許手動清理令牌接口
        filterMap.put("/xiaozhi/ota", "anon"); // 允許OTA接口公開訪問
        filterMap.put("/xiaozhi/ota/**", "anon"); // 允許OTA相關接口
        filterMap.put("/ota/**", "anon"); // 允許OTA相關接口
        
        // 允許用戶註冊公開訪問
        filterMap.put("/sys/user/register", "anon");
        
        filterMap.put("/**", "oauth2");
        shiroFilter.setFilterChainDefinitionMap(filterMap);

        return shiroFilter;
    }

    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}