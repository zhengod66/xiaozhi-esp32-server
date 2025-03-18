package xiaozhi.modules.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xiaozhi.modules.security.config.JwtConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具類
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     *
     * @param deviceId   設備ID
     * @param macAddress 設備MAC地址
     * @param expiration 過期時間（秒）
     * @return JWT令牌
     */
    public String generateToken(Long deviceId, String macAddress, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("deviceId", deviceId);
        claims.put("macAddress", macAddress);

        return Jwts.builder()
                .claims(claims)
                .subject(deviceId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成JWT令牌（使用配置的默認過期時間）
     *
     * @param deviceId   設備ID
     * @param macAddress 設備MAC地址
     * @return JWT令牌
     */
    public String generateToken(Long deviceId, String macAddress) {
        return generateToken(deviceId, macAddress, jwtConfig.getExpiration());
    }

    /**
     * 從JWT令牌中獲取設備ID
     *
     * @param token JWT令牌
     * @return 設備ID
     */
    public Long getDeviceIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return Long.parseLong(claims.getSubject());
        }
        return null;
    }

    /**
     * 從JWT令牌中獲取MAC地址
     * 
     * @param token JWT令牌
     * @return MAC地址
     */
    public String getMacAddressFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("macAddress", String.class);
        }
        return null;
    }

    /**
     * 驗證JWT令牌
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("JWT令牌簽名無效: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT令牌已過期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌為空或無效: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return 聲明
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("無法解析JWT令牌: {}", e.getMessage());
            return null;
        }
    }
} 