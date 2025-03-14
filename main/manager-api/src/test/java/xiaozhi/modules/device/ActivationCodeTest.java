package xiaozhi.modules.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 激活码服务简单测试 - 不依赖Spring上下文
 */
public class ActivationCodeTest {

    /**
     * 简单的冒烟测试，验证测试环境是否可用
     */
    @Test
    public void testSmoke() {
        // 简单的断言，用于验证测试环境
        assertTrue(true, "基本断言应该通过");
        assertEquals("123456", "12" + "3456", "字符串连接应该正确");
    }
} 