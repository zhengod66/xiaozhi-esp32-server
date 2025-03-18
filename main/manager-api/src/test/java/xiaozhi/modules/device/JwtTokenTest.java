package xiaozhi.modules.device;

import org.junit.jupiter.api.Test;
import xiaozhi.modules.security.config.JwtConfig;
import xiaozhi.modules.security.jwt.JwtTokenProvider;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT令牌單元測試
 */
public class JwtTokenTest {

    /**
     * 測試JWT令牌生成和驗證
     */
    @Test
    public void testJwtTokenGeneration() {
        // 準備測試數據
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test_secret_key_for_jwt_token_test_case_execution");
        
        JwtTokenProvider tokenProvider = new JwtTokenProvider(jwtConfig);
        
        Long deviceId = 1L;
        String macAddress = "00:11:22:33:44:55";
        Long expiration = 3600L; // 1小時
        
        // 生成令牌
        String token = tokenProvider.generateToken(deviceId, macAddress, expiration);
        
        // 斷言令牌不為空
        assertNotNull(token, "生成的令牌不應為空");
        
        // 驗證令牌
        assertTrue(tokenProvider.validateToken(token), "令牌應該是有效的");
        
        // 從令牌中獲取設備ID
        Long extractedDeviceId = tokenProvider.getDeviceIdFromToken(token);
        assertEquals(deviceId, extractedDeviceId, "從令牌中提取的設備ID應該與原始ID相同");
        
        // 從令牌中獲取MAC地址
        String extractedMacAddress = tokenProvider.getMacAddressFromToken(token);
        assertEquals(macAddress, extractedMacAddress, "從令牌中提取的MAC地址應該與原始地址相同");
        
        System.out.println("JWT令牌測試成功！");
    }
} 