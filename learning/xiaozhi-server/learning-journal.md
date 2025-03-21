# Xiaozhi Server 學習日誌

> 本文件記錄了Xiaozhi Server (Python/FastAPI)開發過程中遇到的問題和解決方案

## WebSocket連接認證

**問題描述**: 設備無法建立或維持WebSocket連接，服務器日誌顯示認證失敗

**症狀**: 
- 連接嘗試被拒絕
- 日誌中顯示401錯誤
- 設備反復嘗試重連

**解決方案**:
- 實現基於JWT的認證機制
- 確保令牌包含設備ID和權限信息
- 添加詳細的認證日誌

**預防措施**:
- 添加詳細的認證日誌和監控
- 建立令牌生命週期管理機制
- 定期輪換令牌

## 激活碼安全性

**問題描述**: 潛在的激活碼猜測攻擊風險

**症狀**:
- 短時間內大量激活嘗試
- 來自單一IP的多次嘗試

**解決方案**:
- 增加激活碼長度和複雜度
- 添加使用次數限制和IP限制
- 實現激活碼速率限制

**預防措施**:
- 實現暴力破解檢測機制
- 監控異常激活嘗試
- 設置激活碼有效期限制

## RESTful API標準化與統一響應格式

**問題描述**: API設計不一致，缺乏標準化的響應格式和HTTP方法使用

**症狀**: 
- 不同API返回格式不一致
- API路徑命名不規範
- HTTP方法使用不符合RESTful規範
- 缺少版本管理策略

**解決方案**:
- 創建統一的`ApiResponse<T>`響應類，包含成功標誌、狀態碼、消息和數據
- 按照RESTful設計原則重構API：
  - GET方法用於查詢
  - POST方法用於創建
  - PUT方法用於更新
  - DELETE方法用於刪除
- 引入API版本管理，新API使用`/api/v1/`前綴
- 保留舊API並標記為`@Deprecated`以保持向後兼容

**預防措施**:
- 建立API設計規範文檔
- 使用Swagger/Knife4j進行API文檔自動生成
- 實施API審核流程

## 數據庫字段與實體類不匹配問題

**問題描述**: Java實體類中的方法名稱與數據庫表字段名稱不匹配

**症狀**:
- 編譯錯誤：`The method setUpdateTime(Date) is undefined for the type DeviceEntity`
- OTA API調用失敗

**解決方案**:
- 分析實體類與數據庫結構，發現數據庫使用`update_date`而代碼調用`getUpdateTime`
- 修改`DeviceServiceImpl`類中的方法調用，將`getUpdateTime`改為`getUpdateDate`
- 確保所有相關類和方法使用統一的命名規則

**預防措施**:
- 維護統一的數據庫設計文檔
- 使用清晰的命名約定：數據庫用`create_date`和`update_date`，實體屬性用`createDate`和`updateDate`
- 避免混合使用`Time`和`Date`後綴
- 考慮使用JPA實體生成工具或ORM框架自動映射機制

## Swagger配置優化與JWT認證支持

**問題描述**: Swagger UI無法添加認證令牌，影響API測試

**症狀**:
- 無法在Swagger UI中添加認證頭
- 需要通過Postman等外部工具測試需要認證的API

**解決方案**:
- 更新`SwaggerConfig`類，添加JWT安全方案定義
- 配置OpenAPI的全局安全需求，添加Bearer令牌支持
- 指定JWT令牌格式，方便開發人員理解如何使用

**預防措施**:
- 在API文檔中明確說明認證要求
- 提供完整的認證示例
- 考慮添加開發環境的測試令牌

## Knife4j 文檔 JSON 解析錯誤問題

**問題描述**: 訪問 Knife4j 文檔頁面時出現 JSON 解析錯誤

**症狀**: 
- 錯誤信息: `SyntaxError: Unexpected token 'e', "eyJvcGVuYX"... is not valid JSON`
- 文檔頁面無法正常加載
- 返回的數據看起來像是 Base64 編碼的 JWT 令牌，而非 JSON 格式

**解決方案**:

1. **問題根源:** `/v3/api-docs` 端點返回 Base64 編碼的內容，而不是預期的 JSON。這是因為 Knife4j 的 JWT 功能在某些情況下仍然生效，即使在配置中已經禁用。

2. **實施以下修復方案:**

   a. **創建 API 文檔響應包裝過濾器：** 直接攔截 API 文檔請求並確保返回有效的 JSON。
   ```java
   @Component
   @Order(Ordered.HIGHEST_PRECEDENCE)
   public class ApiDocResponseWrapper extends OncePerRequestFilter {
       
       // 擴大攔截範圍，處理所有文檔相關路徑
       private static final Pattern API_DOCS_PATTERN = Pattern.compile("(/v3/api-docs.*)|(/swagger-ui/.*)|(/doc\\.html.*)");
       
       // 對主端點特殊處理 - 直接返回預先準備的有效 JSON
       if ("/xiaozhi-esp32-api/v3/api-docs".equals(path)) {
           log.info("檢測到主 API 文檔請求，直接返回有效的 JSON");
           String json = createMasterOpenApiJson();
           response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
           return;
       }
       
       // 對其他文檔路徑進行內容檢查
       if (responseText.startsWith("ey")) {
           log.warn("檢測到 Base64 編碼的 JWT 令牌，嘗試解碼");
           
           try {
               // 嘗試解碼 Base64
               byte[] decodedBytes = java.util.Base64.getDecoder().decode(responseText);
               // ... 處理解碼內容 ...
           }
       }
   }
   ```

   b. **更新 application-dev.yml 配置：** 徹底禁用可能導致問題的功能。
   ```yaml
   knife4j:
     enable: true
     production: false
     setting:
       enableSwaggerModels: true
       swaggerModelName: 數據模型
       enableOpenApi: true
       enableRequestCache: false  # 禁用請求緩存
       enableJwt: false          # 明確禁用 JWT
       enableCrossDomain: true
       enableFilterMultipartApis: false
       language: zh-CN
   
   springdoc:
     api-docs:
       fallback-to-basic: true  # 啟用基本回退
       default-consumes-media-type: application/json
       default-produces-media-type: application/json
     cache:
       disabled: true  # 禁用緩存 
   ```

   c. **創建預設的 API 文檔 JSON 輸出：** 提供有效的備用 JSON，當原始響應無效時使用。
   ```java
   private String createMasterOpenApiJson() throws IOException {
       Map<String, Object> openapi = new HashMap<>();
       openapi.put("openapi", "3.0.1");
       
       Map<String, String> info = new HashMap<>();
       info.put("title", "小智ESP32 API");
       info.put("description", "設備管理系統API文檔");
       openapi.put("info", info);
       
       // 添加基本路徑結構和標籤
       openapi.put("paths", new HashMap<>());
       openapi.put("tags", java.util.List.of(
               Map.of("name", "device", "description", "設備管理"),
               Map.of("name", "system", "description", "系統管理")
       ));
       
       return objectMapper.writeValueAsString(openapi);
   }
   ```

   d. **提供詳細的日誌記錄：** 添加更多日誌輸出，協助診斷問題。
   ```java
   log.info("API 文檔原始響應 (前100字符): {}", 
       responseText.length() > 100 ? responseText.substring(0, 100) + "..." : responseText);
       
   log.info("解碼後的內容 (前100字符): {}", 
       decodedText.length() > 100 ? decodedText.substring(0, 100) + "..." : decodedText);
   ```

3. **關鍵經驗:**
   - Base64 問題的關鍵標誌是響應內容以 `ey` 開頭，這是 Base64 編碼的 JWT 令牌的典型特徵
   - 即使在配置中禁用了 JWT 功能，某些情況下它仍可能生效
   - 對於分組 API 文檔 (`/v3/api-docs/{groupName}`)，需要特別處理每個分組的文檔格式
   - 必須確保所有文檔相關路徑都被過濾器覆蓋，包括 `/swagger-ui/` 和 `/doc.html`

4. **後續預防措施:**
   - 持續監控 API 文檔請求的響應格式
   - 考慮實現簡化的開發時備用文檔界面
   - 使用 `application-prod.yml` 中禁用 Knife4j 或將 `knife4j.production` 設置為 `true`，在生產環境中避免這類問題
   - 定期檢查 Knife4j 和 SpringDoc 的更新，及時跟進新版本中可能修復的相關問題

**預防措施**:
- 在設計 API 時考慮文檔生成框架的兼容性
- 實現自定義的文檔輸出攔截器，確保返回有效的 JSON
- 確保所有文檔路徑都被 Shiro 配置允許匿名訪問
- 保持詳細的日誌記錄以便快速識別問題

## JWT認證和WebSocket連接管理實現

**問題描述**: 需要在Xiaozhi Server實現基於JWT的WebSocket認證機制，確保只有持有有效令牌的設備才能建立連接

**症狀**:
- 未經授權的設備可以自由連接WebSocket服務
- 無法識別和跟踪不同設備的連接状态
- 缺乏主動断开和管理连接的机制

**解決方案**:

1. **共享JWT密钥实现**:
   - 创建共享配置文件夹，存储JWT密钥
   ```properties
   # shared-config/jwt-secret.properties
   jwt.secret=xiaozhi_esp32_jwt_secret_key_for_device_authentication_system
   jwt.algorithm=HS384
   ```
   
   - 创建JWT配置加载器，从共享文件读取密钥
   ```python
   class JwtConfig:
       def __init__(self):
           self.secret = "默认密钥"
           self.algorithm = "HS384"
           
           # 尝试从共享配置加载
           try:
               base_path = Path(__file__).parent.parent.parent.parent
               config_path = base_path / "shared-config" / "jwt-secret.properties"
               
               if config_path.exists():
                   with open(config_path, 'r', encoding='utf-8') as f:
                       for line in f:
                           if '=' in line and not line.strip().startswith('#'):
                               key, value = line.strip().split('=', 1)
                               if key == "jwt.secret":
                                   self.secret = value
                               elif key == "jwt.algorithm":
                                   self.algorithm = value
           except Exception as e:
               logging.error(f"加载JWT配置失败: {str(e)}")
   ```

2. **JWT验证工具类实现**:
   - 创建JWT解析和验证工具类
   ```python
   class JwtHelper:
       @staticmethod
       def decode_token(token: str):
           try:
               # 使用共享密钥解码JWT
               payload = jwt.decode(
                   token, 
                   jwt_config.secret, 
                   algorithms=[jwt_config.algorithm]
               )
               
               # 验证必要字段
               if "deviceId" not in payload:
                   raise JwtMissingFieldError("deviceId")
                   
               if "macAddress" not in payload:
                   raise JwtMissingFieldError("macAddress")
               
               # 检查令牌是否过期
               if JwtHelper.is_token_expired(payload):
                   raise JwtExpiredError()
                   
               return payload
           except jwt.ExpiredSignatureError:
               logging.warning("令牌已过期")
               raise JwtExpiredError()
           except jwt.InvalidTokenError as e:
               logging.warning(f"无效的令牌: {str(e)}")
               raise JwtInvalidError(str(e))
   ```

3. **连接管理器实现**:
   - 创建WebSocket连接管理器，跟踪设备连接状态
   ```python
   class ConnectionManager:
       def __init__(self):
           # 活跃连接映射 {device_id: {websocket, ...}}
           self.active_connections = {}
           # 设备状态映射 {device_id: status}
           self.device_status = {}
           # 连接时间映射 {device_id: connect_time}
           self.connection_times = {}
           
       async def connect(self, websocket, device_id):
           if device_id not in self.active_connections:
               self.active_connections[device_id] = set()
           
           self.active_connections[device_id].add(websocket)
           self.device_status[device_id] = "online"
           self.connection_times[device_id] = time.time()
           logging.info(f"设备 {device_id} 已连接")
   ```

4. **WebSocket认证流程实现**:
   - 在WebSocket处理函数中添加认证逻辑
   ```python
   async def _handle_connection(self, websocket):
       try:
           # 获取令牌
           token = None
           try:
               params = websocket.path.split("?", 1)[1] if "?" in websocket.path else ""
               pairs = [p.split("=", 1) for p in params.split("&") if "=" in p]
               query_params = {key: value for key, value in pairs}
               token = query_params.get("token")
           except Exception:
               pass
               
           if not token:
               await websocket.close(code=1008, reason="缺少认证令牌")
               return
               
           # 验证令牌
           is_valid, payload, error = JwtHelper.validate_token(token)
           if not is_valid:
               await websocket.close(code=1008, reason=error)
               return
               
           # 提取设备信息
           device_id, mac_address = JwtHelper.get_device_info(payload)
           
           # 更新连接状态
           await connection_manager.connect(websocket, device_id)
           
           # 后续处理连接...
       except Exception as e:
           logging.error(f"处理WebSocket连接出错: {str(e)}")
   ```

5. **REST API接口实现**:
   - 添加设备状态查询和管理API
   ```python
   @app.get("/api/devices")
   async def get_all_devices():
       online_devices = connection_manager.get_online_devices()
       return {"devices": list(online_devices.items())}
       
   @app.post("/api/devices/{device_id}/disconnect")
   async def disconnect_device(device_id: str):
       if not connection_manager.is_device_online(device_id):
           raise HTTPException(status_code=404)
       await connection_manager.close_connection(device_id)
       return {"message": "设备已断开连接"}
   ```

**預防措施**:
- 定期更新JWT密钥，提高安全性
- 添加详细日志记录设备连接和断开事件
- 实现连接状态监控，及时发现异常
- 配置合理的令牌有效期，平衡安全和用户体验
- 使用环境变量或配置服务更安全地管理密钥

**最佳实践**:
- 使用共享配置文件简化多服务部署，确保密钥一致性
- 对JWT令牌验证失败进行详细的错误分类和处理
- 实现设备连接状态实时监控
- 提供API接口进行设备连接管理
- 妥善处理WebSocket连接异常，确保资源正确释放

## ESP32设备Authorization头部JWT认证

**問題描述**: ESP32设备使用Authorization头部发送JWT令牌，但服务器只从URL查询参数获取令牌，导致认证失败

**症狀**: 
- 连接错误: "WebSocket连接尝试缺少认证令牌"
- 服务器日志显示: `WebSocket连接尝试缺少认证令牌`
- ESP32设备无法连接服务器

**解決方案**:
1. **识别问题根源**:
   - ESP32设备代码通过`Authorization`头部发送令牌:
   ```cpp
   websocket_->SetHeader("Authorization", token.c_str());
   ```
   
   - 但服务器只从URL查询参数获取令牌:
   ```python
   params = websocket.path.split("?", 1)[1] if "?" in websocket.path else ""
   pairs = [p.split("=", 1) for p in params.split("&") if "=" in p]
   query_params = {key: value for key, value in pairs}
   token = query_params.get("token")
   ```

2. **修改服务器代码，支持从Authorization头部获取令牌**:
   ```python
   # 如果URL参数中没有token，尝试从Authorization头部获取
   if not token:
       # 使用getattr安全地获取属性，避免属性不存在导致的错误
       headers = getattr(websocket, "headers", None)
       if headers:
           auth_header = headers.get("Authorization", "")
           if auth_header.startswith("Bearer "):
               token = auth_header[7:]  # 移除 "Bearer " 前缀
       else:
           self.logger.info("无法从连接对象获取请求头")
   ```

**預防措施**:
- 服务器应支持多种令牌传递方式，增强兼容性
- 详细记录认证失败的具体原因
- 在服务器启动日志中明确说明支持的认证方式
- 在文档中清晰说明客户端可以使用的认证方法

**最佳实践**:
- 服务器和客户端应支持标准的JWT传递方式：
  1. Authorization头部: `Authorization: Bearer <token>`
  2. URL查询参数: `?token=<token>`
- 服务器应详细记录认证过程的每个步骤，方便调试
- 认证失败时应返回具体原因，帮助客户端排查问题

## WebSocket服务器端口配置

**問題描述**: 需要将WebSocket服务器配置为使用特定端口(8000)，而不是默认端口

**症狀**: 
- ESP32设备无法连接到服务器
- 设备可能配置为连接特定端口
- 防火墙规则可能只允许特定端口

**解決方案**:
1. **修改test_config.yaml文件**:
   ```yaml
   server:
     ip: 0.0.0.0
     port: 8000  # 将端口从8765更改为8000
   ```

2. **确保一致性**:
   - 确保ESP32客户端代码中的连接URL使用相同的端口
   ```cpp
   // ESP32客户端代码示例
   const char* websocket_server_host = "your-server-ip";
   const int websocket_server_port = 8000;
   ```

**預防措施**:
- 在项目文档中明确记录服务器使用的端口
- 确保所有客户端配置与服务器配置保持一致
- 考虑使用环境变量或配置文件使端口配置更灵活
- 确保防火墙规则允许选定的端口

**最佳实践**:
- 使用配置文件而不是硬编码的方式设置端口
- 服务器启动时清晰记录正在使用的端口
- 在连接失败时提供有关端口配置的明确错误消息
- 不同环境(开发、测试、生产)可以使用不同的端口配置

## WebSocket请求头访问错误

**問題描述**: 在WebSocket服务器处理连接时，尝试访问不存在的`request_headers`属性导致错误

**症狀**: 
- 服务器日志错误: `AttributeError: 'ServerConnection' object has no attribute 'request_headers'`
- 设备无法认证并连接到服务器
- 即使设备通过Authorization头提供了JWT令牌，也无法被服务器识别

**解決方案**:
1. **识别问题根源**:
   - websockets库版本更新后，`ServerConnection`对象可能不再直接提供`request_headers`属性
   - 在连接处理代码中使用了可能不存在的属性

2. **修改连接处理代码**:
   ```python
   # 旧代码，容易出错:
   if not token and websocket.request_headers:
       auth_header = websocket.request_headers.get("Authorization", "")
       
   # 新代码，更安全:
   if not token:
       # 使用getattr安全地获取属性，避免属性不存在导致的错误
       headers = getattr(websocket, "headers", None)
       if headers:
           auth_header = headers.get("Authorization", "")
           if auth_header.startswith("Bearer "):
               token = auth_header[7:]  # 移除 "Bearer " 前缀
       else:
           self.logger.info("无法从连接对象获取请求头")
   ```

**預防措施**:
- 使用`getattr()`安全获取对象属性，提供默认值处理不存在的情况
- 加强异常处理和日志记录，确保捕获并明确记录属性访问错误
- 当使用第三方库时，关注API变更并及时更新代码
- 在升级依赖库版本后进行完整测试

**最佳实践**:
- 定期检查websockets库的文档和更新日志，了解API变更
- 实现更健壮的令牌获取机制，支持多种方式传递认证信息
- 在处理不同版本库时使用更防御性的编程方式
- 增加详细的调试日志，帮助识别类似的问题

_使用說明: 遇到新問題時，按照上述格式添加條目_ 

## Loguru日志格式错误

**問題描述**: Loguru日志格式中引用了不存在的`tag`字段，导致日誌記錄錯誤

**症狀**: 
- 日志错误: `KeyError: 'tag'`
- 跟踪栈显示错误出现在`loguru._handler.py`的`emit`方法中
- 格式化错误: `formatted = precomputed_format.format_map(formatter_record)`

**解決方案**:
1. **检查日志配置**:
   - 检查日志格式配置中是否引用了`{tag}`字段
   - 修改日志配置，使用一个安全的格式字符串，确保所有字段都存在

2. **修复日志记录代码**:
   ```python
   # 旧代码，可能导致错误:
   self.logger.info("无法从连接对象获取请求头")
   
   # 新代码，为所有日志记录提供tag字段:
   self.logger.bind(tag=TAG).info("无法从连接对象获取请求头")
   ```

3. **或者更新全局日志格式**:
   ```python
   # 在config/logger.py中修改日志格式，移除{tag}或提供默认值
   logger.configure(
       handlers=[
           {
               "sink": sys.stdout,
               "format": "<level>{level}</level> | <green>{time:YYYY-MM-DD HH:mm:ss}</green> | {message}",
           }
       ]
   )
   ```

**預防措施**:
- 确保日志格式字符串中的所有字段在记录日志时都有提供
- 使用`bind()`方法添加额外字段时保持一致性
- 考虑为常用字段设置默认值，防止格式化错误

**最佳实践**:
- 创建一个统一的日志格式配置，确保兼容所有模块
- 使用结构化日志，便于解析和查询
- 定期检查日志输出，确保没有格式错误

## WebSocket Hello消息格式不匹配问题

### 问题描述
ESP32客户端在建立WebSocket连接后，发送"hello"消息并等待服务器回应相同格式的"hello"消息，但服务器发送的是"welcome"格式的消息，导致客户端无法正确识别服务器的响应，报错"Failed to receive server hello"。

### 症状
- ESP32客户端日志显示"Failed to receive server hello"
- WebSocket连接建立后立即断开
- 客户端等待服务器hello消息超时

### 原因分析
客户端期望的消息格式：
```json
{
  "type": "hello",
  "transport": "websocket",
  "audio_params": {
    "sample_rate": 16000
  }
}
```

服务器发送的消息格式：
```json
{
  "type": "welcome",
  "session_id": "uuid",
  "message": "欢迎连接到小智ESP32服务器"
}
```

这两种格式不匹配，导致客户端无法识别服务器的响应。

### 解决方案
1. 修改服务器的欢迎消息格式，使其与客户端期望的格式匹配：
   ```python
   # 在connection.py中：
   self.welcome_msg = {
       "type": "hello",
       "transport": "websocket",
       "audio_params": {
           "sample_rate": 16000,
           "format": "opus",
           "channels": 1
       },
       "session_id": self.session_id
   }
   ```

2. 增强`handleHelloMessage`函数，确保hello消息格式正确：
   ```python
   # 确保包含必要的字段
   if "type" not in conn.welcome_msg or conn.welcome_msg["type"] != "hello":
       conn.welcome_msg["type"] = "hello"
   
   if "transport" not in conn.welcome_msg:
       conn.welcome_msg["transport"] = "websocket"
   
   if "audio_params" not in conn.welcome_msg:
       conn.welcome_msg["audio_params"] = {
           "sample_rate": 16000,
           "format": "opus",
           "channels": 1
       }
   ```

### 预防措施
1. 在开发阶段明确定义并记录客户端和服务器之间的协议格式
2. 对特定设备类型的消息格式进行单元测试
3. 为不同类型的客户端实现专门的消息处理器，以适应不同的协议需求
4. 添加验证逻辑，确保发送的消息符合协议规范

### 学习经验
WebSocket通信协议需要客户端和服务器双方达成一致。当支持多种客户端时，服务器应该能够识别不同客户端的类型，并以适当的格式回复消息。在本例中，ESP32客户端有特定的消息格式要求，服务器需要遵循这些要求才能建立成功的通信。

### 备注
这种类型的协议不匹配问题可能是微妙的，因为连接可能会建立，但随后由于消息格式不匹配而失败。详细的日誌記錄對於診斷類似問題至關重要。

## 测试配置导致的模块初始化错误

**问题描述**: 使用标准启动命令时，系统仍加载测试配置文件，导致模块初始化错误

**症状**: 
- 服务器日志显示错误: `主程序运行出错: 不支持的SileroVAD类型: Silero`
- 即使使用`--config_path config.yaml`参数，系统仍加载测试配置
- 测试配置文件中的模块配置不完整，导致初始化失败

**原因分析**:
- `app_auth_with_api.py`中的代码会优先检查测试配置文件是否存在，如果存在就直接使用：
```python
test_config_path = os.path.join(os.path.dirname(__file__), "test_config.yaml")
if os.path.exists(test_config_path):
    logger.info(f"使用测试配置文件: {test_config_path}")
    with open(test_config_path, 'r', encoding='utf-8') as f:
        config = yaml.safe_load(f)
else:
    # 加载默认配置
    logger.info("测试配置文件不存在，使用默认配置")
    config = load_config()
```

**解决方案**:
1. **临时解决方法**:
   - 重命名或移除测试配置文件，强制系统使用标准配置：
   ```bash
   # 将测试配置文件重命名，这样程序就找不到它了
   ren test_config.yaml test_config.yaml.bak
   
   # 然后再次运行程序
   python app_auth_with_api.py
   ```

2. **修改测试配置内容**:
   - 为测试配置添加必要的VAD配置:
   ```yaml
   VAD:
     Silero:
       model_path: "data/silero_vad.onnx"
       threshold: 0.5
       sampling_rate: 16000
       window_size_samples: 1536
   ```
   
   - 或者禁用不需要的模块:
   ```yaml
   disable_vad: true
   disable_asr: true
   disable_llm: false
   disable_tts: false
   disable_memory: false
   disable_intent: true
   ```

3. **长期解决方法**:
   - 修改`app_auth_with_api.py`以尊重命令行参数，使其优先于测试配置文件

**预防措施**:
- 在配置选择逻辑中添加更清晰的优先级规则
- 确保测试配置文件包含完整的模块配置
- 添加详细的启动日志，清楚显示使用的是哪个配置文件
- 为不同环境（开发、测试、生产）维护单独的配置文件

**最佳实践**:
- 使用命令行参数或环境变量控制配置文件选择
- 为测试配置提供与生产配置相同结构的完整配置
- 分别测试所有配置文件，确保它们都能正常工作
- 在代码中添加配置验证逻辑，检查必要的配置项是否存在

## JWT令牌验证与WebSocket连接认证

**问题描述**: 确认JWT令牌是否能被服务器正确验证，以及如何在WebSocket连接过程中传递和验证令牌

**症状**: 
- 需要确认JWT令牌验证逻辑是否正确
- 不确定令牌传递方式的选择（URL参数还是Headers）
- 难以判断认证失败的具体原因

**验证过程**:
1. **令牌内容分析**:
   - 使用JWT调试工具解析令牌内容:
   ```bash
   python utils/jwt_debug.py [JWT令牌]
   ```
   
   - 确认令牌包含必要字段:
     - `deviceId`: 设备ID
     - `macAddress`: MAC地址
     - `exp`: 过期时间戳
     - `iat`: 签发时间戳

2. **令牌签名验证**:
   - 使用密钥验证令牌签名:
   ```bash
   python utils/jwt_debug.py [JWT令牌] --verify --config ../../shared-config/jwt-secret.properties
   ```
   
   - 或直接提供密钥:
   ```bash
   python utils/jwt_debug.py [JWT令牌] --verify --secret xiaozhi_esp32_jwt_secret_key_for_device_authentication_system
   ```

3. **传递方式选择**:
   - URL查询参数方式（推荐用于ESP32）:
   ```
   ws://your-server:8000/ws?token=eyJhbGciOiJIUzM4NCJ9...
   ```
   
   - Authorization头部方式（标准但ESP32支持有限）:
   ```
   websocket_->SetHeader("Authorization", "Bearer eyJhbGciOiJIUzM4NCJ9...");
   ```

**解决方案**:
1. 服务器已实现对两种传递方式的支持:
   - 先尝试从URL参数获取令牌
   - 如果不存在，再尝试从Authorization头部获取
   - 详细记录每个步骤，便于调试

2. 为ESP32客户端推荐使用URL参数方式:
   ```cpp
   // 构建带有令牌的WebSocket URL
   String wsUrl = String("ws://") + serverHost + ":" + serverPort + "/ws?token=" + jwtToken;
   // 连接WebSocket
   websocket.begin(wsUrl);
   ```

3. 服务器处理代码已添加详细日志记录:
   - 记录请求路径和查询参数
   - 记录令牌获取方式和来源
   - 记录验证结果和设备信息

**预防措施**:
- 使用JWT调试工具定期验证令牌格式和内容
- 为不同环境维护不同的JWT密钥
- 实现令牌过期和刷新机制
- 建立连接状态监控，及时发现认证问题

**最佳实践**:
- 在服务器和客户端之间统一令牌传递方式
- 为认证失败提供明确的错误消息
- 保持合适的令牌有效期平衡安全和用户体验
- 使用标准的JWT结构和验证流程