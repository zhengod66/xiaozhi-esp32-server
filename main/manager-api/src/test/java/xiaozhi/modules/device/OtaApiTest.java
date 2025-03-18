package xiaozhi.modules.device;

import org.junit.jupiter.api.Test;
import xiaozhi.modules.device.dto.OtaRequestDTO;
import xiaozhi.modules.device.dto.OtaResponseDTO;
import xiaozhi.modules.device.config.OtaConfig;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.device.service.ActivationCodeService;
import xiaozhi.modules.device.service.AccessTokenService;
import xiaozhi.modules.device.service.impl.OtaServiceImpl;
import xiaozhi.modules.device.constant.DeviceConstant;
import xiaozhi.modules.device.dto.DeviceDTO;
import xiaozhi.modules.device.dto.ActivationCodeDTO;
import xiaozhi.modules.device.dto.AccessTokenDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;

/**
 * OTA API測試
 */
public class OtaApiTest {

    /**
     * 簡單的冒煙測試，驗證測試環境是否可用
     */
    @Test
    public void testSmoke() {
        // 簡單的斷言，用於驗證測試環境
        assertTrue(true, "基本斷言應該通過");
    }

    /**
     * 測試處理未註冊設備請求
     */
    @Test
    public void testProcessOtaRequestForNewDevice() {
        // 創建模擬服務
        DeviceService deviceService = mock(DeviceService.class);
        ActivationCodeService activationCodeService = mock(ActivationCodeService.class);
        AccessTokenService accessTokenService = mock(AccessTokenService.class);
        OtaConfig otaConfig = new OtaConfig();
        
        // 配置模擬行為
        DeviceDTO savedDevice = new DeviceDTO();
        savedDevice.setId(1L);
        savedDevice.setMacAddress("00:11:22:33:44:55");
        savedDevice.setClientId("test-client-id");
        savedDevice.setStatus(DeviceConstant.Status.INACTIVE);
        
        // 模擬設備服務行為 - 首次查询不存在，保存后再查询时返回设备
        when(deviceService.getByMacAddress("00:11:22:33:44:55"))
            .thenReturn(null)  // 第一次查询返回null
            .thenReturn(savedDevice);  // 第二次查询返回已保存的设备
        
        // 模擬激活碼生成
        ActivationCodeDTO activationCode = new ActivationCodeDTO();
        activationCode.setCode("123456");
        activationCode.setDeviceId(1L);
        activationCode.setStatus(DeviceConstant.ActivationStatus.VALID);
        activationCode.setExpireTime(new Date(System.currentTimeMillis() + 1800000)); // 30分鐘後
        
        when(activationCodeService.generateCode(eq(1L), anyInt())).thenReturn(activationCode);
        
        // 創建OTA服務實例
        OtaServiceImpl otaService = new OtaServiceImpl(deviceService, activationCodeService, accessTokenService, otaConfig);
        
        // 創建測試請求
        OtaRequestDTO request = new OtaRequestDTO();
        request.setMacAddress("00:11:22:33:44:55");
        request.setClientId("test-client-id");
        request.setDeviceType("ESP32");
        request.setFirmwareVersion("1.0.0");
        
        // 執行測試 - 使用processOtaRequest方法而不是直接调用handleUnregisteredDevice
        OtaResponseDTO response = otaService.processOtaRequest(request);
        
        // 驗證結果
        assertNotNull(response, "響應不應為空");
        assertNotNull(response.getActivation(), "激活信息不應為空");
        assertEquals("123456", response.getActivation().getCode(), "激活碼應正確返回");
        assertNotNull(response.getActivation().getMessage(), "激活消息不應為空");
        
        // 驗證固件和服務器時間信息
        assertNotNull(response.getFirmware(), "固件信息不應為空");
        assertNotNull(response.getServer_time(), "服務器時間不應為空");
        
        // 验证方法调用
        verify(deviceService, times(2)).getByMacAddress("00:11:22:33:44:55");
        verify(deviceService).save(any(DeviceDTO.class));
        verify(activationCodeService).generateCode(eq(1L), anyInt());
    }

    /**
     * 測試處理已激活設備請求
     */
    @Test
    public void testHandleActiveDevice() {
        // 創建模擬服務
        DeviceService deviceService = mock(DeviceService.class);
        ActivationCodeService activationCodeService = mock(ActivationCodeService.class);
        AccessTokenService accessTokenService = mock(AccessTokenService.class);
        OtaConfig otaConfig = new OtaConfig();
        otaConfig.setServer("test-server");
        otaConfig.setPort(8888);
        otaConfig.setPath("/test-ws");
        
        // 模擬訪問令牌
        AccessTokenDTO accessToken = new AccessTokenDTO();
        accessToken.setId(1L);
        accessToken.setDeviceId(1L);
        accessToken.setToken("test-token");
        accessToken.setIsRevoked(DeviceConstant.TokenRevoked.NO);
        accessToken.setExpireTime(new Date(System.currentTimeMillis() + 604800000)); // 7天後
        
        when(accessTokenService.getValidTokenByDeviceId(eq(1L))).thenReturn(accessToken);
        when(accessTokenService.getOrCreateToken(eq(1L))).thenReturn("test-token");
        
        // 創建OTA服務實例
        OtaServiceImpl otaService = new OtaServiceImpl(deviceService, activationCodeService, accessTokenService, otaConfig);
        
        // 執行測試
        OtaResponseDTO response = otaService.handleActiveDevice(1L, "00:11:22:33:44:55");
        
        // 驗證結果
        assertNotNull(response, "響應不應為空");
        assertNotNull(response.getWebsocket(), "WebSocket信息不應為空");
        assertEquals("test-token", response.getWebsocket().getAccess_token(), "訪問令牌應正確返回");
        assertEquals("test-server", response.getWebsocket().getServer(), "WebSocket服務器地址應正確返回");
        assertEquals(Integer.valueOf(8888), response.getWebsocket().getPort(), "WebSocket服務器端口應正確返回");
        assertEquals("/test-ws", response.getWebsocket().getPath(), "WebSocket路徑應正確返回");
        
        // 驗證交互
        verify(accessTokenService).getOrCreateToken(eq(1L));
    }

    /**
     * 測試處理OTA请求時生成的服务器时间和固件信息格式
     */
    @Test
    public void testProcessOtaRequestFormat() {
        // 創建模擬服務
        DeviceService deviceService = mock(DeviceService.class);
        ActivationCodeService activationCodeService = mock(ActivationCodeService.class);
        AccessTokenService accessTokenService = mock(AccessTokenService.class);
        OtaConfig otaConfig = new OtaConfig();
        
        // 模擬設備查詢
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setId(1L);
        deviceDTO.setMacAddress("00:11:22:33:44:55");
        deviceDTO.setClientId("test-client-id");
        deviceDTO.setStatus(DeviceConstant.Status.ACTIVE);
        
        when(deviceService.getByMacAddress(anyString())).thenReturn(deviceDTO);
        when(accessTokenService.getOrCreateToken(eq(1L))).thenReturn("test-token");
        
        // 創建OTA服務實例
        OtaServiceImpl otaService = new OtaServiceImpl(deviceService, activationCodeService, accessTokenService, otaConfig);
        
        // 創建測試請求
        OtaRequestDTO request = new OtaRequestDTO();
        request.setMacAddress("00:11:22:33:44:55");
        request.setClientId("test-client-id");
        request.setDeviceType("ESP32");
        request.setFirmwareVersion("1.0.0");
        
        // 執行測試
        OtaResponseDTO response = otaService.processOtaRequest(request);
        
        // 驗證结果
        assertNotNull(response, "響應不應為空");
        
        // 1. 验证固件信息格式
        assertNotNull(response.getFirmware(), "固件信息不應為空");
        assertNotNull(response.getFirmware().getVersion(), "固件版本不應為空");
        // URL可能為空字串，但不應為null
        assertNotNull(response.getFirmware().getUrl(), "固件URL不應為null");
        
        // 2. 验证服务器时间格式
        assertNotNull(response.getServer_time(), "服務器時間信息不應為空");
        assertNotNull(response.getServer_time().getTimestamp(), "時間戳不應為空");
        assertTrue(response.getServer_time().getTimestamp() > 0, "時間戳應為正數");
        assertNotNull(response.getServer_time().getTimezone_offset(), "時區偏移不應為空");
    }
} 