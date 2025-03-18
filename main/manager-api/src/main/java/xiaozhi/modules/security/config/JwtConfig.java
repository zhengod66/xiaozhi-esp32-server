package xiaozhi.modules.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置類
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    /**
     * 密鑰
     */
    private String secret = "xiaozhi_esp32_jwt_secret_key"; // 默認值，建議在配置文件中更換為更強的隨機值

    /**
     * 過期時間（默認7天，單位：秒）
     */
    private Long expiration = 604800L;

    /**
     * JWT令牌前綴
     */
    private String prefix = "Bearer ";

    /**
     * HTTP請求頭名稱
     */
    private String header = "Authorization";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
} 