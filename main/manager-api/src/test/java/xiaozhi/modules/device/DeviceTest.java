package xiaozhi.modules.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 设备服务简单测试 - 不依赖Spring上下文
 */
public class DeviceTest {

    /**
     * 简单的冒烟测试，验证测试环境是否可用
     */
    @Test
    public void testSmoke() {
        // 简单的断言，用于验证测试环境
        assertTrue(true, "基本断言应该通过");
        assertEquals(4, 2 + 2, "基本数学运算应该正确");
    }
} 