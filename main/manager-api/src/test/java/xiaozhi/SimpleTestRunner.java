package xiaozhi; 
 
import xiaozhi.modules.device.ActivationCodeTest; 
import xiaozhi.modules.device.DeviceTest; 
 
public class SimpleTestRunner { 
    public static void main(String[] args) { 
        System.out.println("========== 開始運行基本測試 =========="); 
 
        try { 
            System.out.println("\n----- 運行設備測試 -----"); 
            DeviceTest deviceTest = new DeviceTest(); 
            deviceTest.testSmoke(); 
            System.out.println("設備測試通過！"); 
        } catch (Exception e) { 
            System.out.println("設備測試失敗：" + e.getMessage()); 
            e.printStackTrace(); 
        } 
 
        try { 
            System.out.println("\n----- 運行激活碼測試 -----"); 
            ActivationCodeTest activationCodeTest = new ActivationCodeTest(); 
            activationCodeTest.testSmoke(); 
            System.out.println("激活碼測試通過！"); 
        } catch (Exception e) { 
            System.out.println("激活碼測試失敗：" + e.getMessage()); 
            e.printStackTrace(); 
        } 
 
        System.out.println("\n========== 測試完成 =========="); 
    } 
} 
