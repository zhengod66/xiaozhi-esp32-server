package xiaozhi;

import xiaozhi.modules.device.ActivationCodeTest;
import xiaozhi.modules.device.DeviceTest;
import xiaozhi.modules.device.JwtTokenTest;
import xiaozhi.modules.device.OtaApiTest;

/**
 * 簡單的測試運行器，用於手動運行測試
 */
public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("========== 開始運行測試 ==========");
        
        // 運行設備測試
        try {
            System.out.println("\n----- 運行設備測試 -----");
            DeviceTest deviceTest = new DeviceTest();
            deviceTest.testSmoke();
            System.out.println("設備測試通過！");
        } catch (Exception e) {
            System.out.println("設備測試失敗：" + e.getMessage());
            e.printStackTrace();
        }
        
        // 運行激活碼測試
        try {
            System.out.println("\n----- 運行激活碼測試 -----");
            ActivationCodeTest activationCodeTest = new ActivationCodeTest();
            activationCodeTest.testSmoke();
            System.out.println("激活碼測試通過！");
        } catch (Exception e) {
            System.out.println("激活碼測試失敗：" + e.getMessage());
            e.printStackTrace();
        }
        
        // 運行JWT令牌測試
        try {
            System.out.println("\n----- 運行JWT令牌測試 -----");
            JwtTokenTest jwtTokenTest = new JwtTokenTest();
            jwtTokenTest.testJwtTokenGeneration();
            System.out.println("JWT令牌測試通過！");
        } catch (Exception e) {
            System.out.println("JWT令牌測試失敗：" + e.getMessage());
            e.printStackTrace();
        }
        
        // 運行OTA API測試
        try {
            System.out.println("\n----- 運行OTA API測試 -----");
            OtaApiTest otaApiTest = new OtaApiTest();
            otaApiTest.testSmoke();
            otaApiTest.testProcessOtaRequestForNewDevice();
            otaApiTest.testHandleActiveDevice();
            otaApiTest.testProcessOtaRequestFormat();
            System.out.println("OTA API測試通過！");
        } catch (Exception e) {
            System.out.println("OTA API測試失敗：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n========== 測試完成 ==========");
    }
} 