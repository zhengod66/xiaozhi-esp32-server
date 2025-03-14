package xiaozhi;

import xiaozhi.modules.device.ActivationCodeTest;
import xiaozhi.modules.device.DeviceTest;

/**
 * 简单的测试运行器，用于手动运行测试
 */
public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("========== 开始运行测试 ==========");
        
        // 运行设备测试
        try {
            System.out.println("\n----- 运行设备测试 -----");
            DeviceTest deviceTest = new DeviceTest();
            deviceTest.testSmoke();
            System.out.println("设备测试通过！");
        } catch (Exception e) {
            System.out.println("设备测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        // 运行激活码测试
        try {
            System.out.println("\n----- 运行激活码测试 -----");
            ActivationCodeTest activationCodeTest = new ActivationCodeTest();
            activationCodeTest.testSmoke();
            System.out.println("激活码测试通过！");
        } catch (Exception e) {
            System.out.println("激活码测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n========== 测试完成 ==========");
    }
} 