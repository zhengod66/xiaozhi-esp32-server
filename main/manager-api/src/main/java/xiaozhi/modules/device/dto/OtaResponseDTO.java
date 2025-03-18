package xiaozhi.modules.device.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * OTA响应数据传输对象
 * 用于构建返回给设备的OTA响应格式
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL) // 不序列化null值字段
public class OtaResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 固件信息
     */
    private FirmwareInfo firmware;

    /**
     * WebSocket连接信息
     */
    private WebsocketInfo websocket;

    /**
     * 激活信息
     */
    private ActivationInfo activation;

    /**
     * 服务器时间信息
     */
    private ServerTimeInfo server_time;

    /**
     * 固件信息
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FirmwareInfo {
        /**
         * 固件版本
         */
        private String version;

        /**
         * 固件下载地址
         */
        private String url;
    }

    /**
     * WebSocket连接信息
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WebsocketInfo {
        /**
         * 访问令牌
         */
        private String access_token;
        
        /**
         * WebSocket服务器地址
         */
        private String server;
        
        /**
         * WebSocket服务器端口
         */
        private Integer port;
        
        /**
         * WebSocket路径
         */
        private String path;
    }

    /**
     * 激活信息
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ActivationInfo {
        /**
         * 激活码
         */
        private String code;
        
        /**
         * 激活消息
         */
        private String message;
    }

    /**
     * 服务器时间信息
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServerTimeInfo {
        /**
         * 时间戳（毫秒）
         */
        private Long timestamp;
        
        /**
         * 时区偏移（分钟）
         */
        private Integer timezone_offset;
    }
    
    /**
     * 创建一个包含固件更新信息的响应
     */
    public static OtaResponseDTO withFirmware(String version, String url) {
        OtaResponseDTO response = new OtaResponseDTO();
        FirmwareInfo firmware = new FirmwareInfo();
        firmware.setVersion(version);
        firmware.setUrl(url);
        response.setFirmware(firmware);
        return response;
    }
    
    /**
     * 创建一个包含激活信息的响应
     */
    public static OtaResponseDTO withActivation(String code, String message) {
        OtaResponseDTO response = new OtaResponseDTO();
        ActivationInfo activation = new ActivationInfo();
        activation.setCode(code);
        activation.setMessage(message);
        response.setActivation(activation);
        return response;
    }
    
    /**
     * 创建一个包含WebSocket信息的响应
     */
    public static OtaResponseDTO withWebsocket(String accessToken, String server, Integer port, String path) {
        OtaResponseDTO response = new OtaResponseDTO();
        WebsocketInfo websocket = new WebsocketInfo();
        websocket.setAccess_token(accessToken);
        websocket.setServer(server);
        websocket.setPort(port);
        websocket.setPath(path);
        response.setWebsocket(websocket);
        return response;
    }
    
    /**
     * 创建一个包含服务器时间信息的响应
     */
    public static OtaResponseDTO withServerTime(Long timestamp, Integer timezoneOffset) {
        OtaResponseDTO response = new OtaResponseDTO();
        ServerTimeInfo serverTime = new ServerTimeInfo();
        serverTime.setTimestamp(timestamp);
        serverTime.setTimezone_offset(timezoneOffset);
        response.setServer_time(serverTime);
        return response;
    }
    
    /**
     * 添加固件信息到现有响应中
     */
    public OtaResponseDTO addFirmware(String version, String url) {
        FirmwareInfo firmware = new FirmwareInfo();
        firmware.setVersion(version);
        firmware.setUrl(url);
        this.setFirmware(firmware);
        return this;
    }
    
    /**
     * 添加WebSocket信息到现有响应中
     */
    public OtaResponseDTO addWebsocket(String accessToken, String server, Integer port, String path) {
        WebsocketInfo websocket = new WebsocketInfo();
        websocket.setAccess_token(accessToken);
        websocket.setServer(server);
        websocket.setPort(port);
        websocket.setPath(path);
        this.setWebsocket(websocket);
        return this;
    }
    
    /**
     * 添加激活信息到现有响应中
     */
    public OtaResponseDTO addActivation(String code, String message) {
        ActivationInfo activation = new ActivationInfo();
        activation.setCode(code);
        activation.setMessage(message);
        this.setActivation(activation);
        return this;
    }
    
    /**
     * 添加服务器时间信息到现有响应中
     */
    public OtaResponseDTO addServerTime(Long timestamp, Integer timezoneOffset) {
        ServerTimeInfo serverTime = new ServerTimeInfo();
        serverTime.setTimestamp(timestamp);
        serverTime.setTimezone_offset(timezoneOffset);
        this.setServer_time(serverTime);
        return this;
    }
} 