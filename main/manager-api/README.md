# 小智ESP32管理後台API

本文檔是開發類文檔，如需部署小智服務端，[點擊這裡查看部署教程](../../README.md#%E4%BD%BF%E7%94%A8%E6%96%B9%E5%BC%8F-)

## 項目介紹

小智ESP32管理後台API (manager-api) 是基於SpringBoot框架開發的後台管理系統，提供用戶認證、權限管理、數據過濾等功能。系統採用分層架構設計，支持Redis緩存、WebSocket通信等特性。

## 技術棧

- **後端框架**：Spring Boot 3.4.3
- **數據庫**：MySQL 8.0+
- **ORM框架**：MyBatis-Plus 3.5.5
- **緩存**：Redis
- **安全框架**：Shiro 2.0.2
- **數據庫連接池**：Druid 1.2.20
- **API文檔**：Knife4j(Swagger) 4.6.0
- **數據庫版本控制**：Liquibase 4.20.0
- **工具庫**：Hutool 5.8.24, Lombok
- **前端框架**：Vue 3.x
- **JWT認證**：JJWT 0.12.5

## 系統架構

項目採用分層架構：
- **表現層**：Controller層處理HTTP請求和響應
- **服務層**：Service層實現業務邏輯
- **數據訪問層**：DAO層與數據庫交互
- **模型層**：Entity實體類定義數據結構

### 核心包結構

```
src/main/java/xiaozhi/
├── AdminApplication.java      # 應用程序入口
├── common/                    # 公共組件
│   ├── annotation/            # 自定義註解
│   ├── aspect/                # AOP切面
│   ├── config/                # 配置類
│   ├── constant/              # 常量定義
│   ├── convert/               # 數據轉換
│   ├── dao/                   # 基礎數據訪問接口
│   ├── entity/                # 基礎實體類
│   ├── exception/             # 異常處理
│   ├── handler/               # 數據處理器
│   ├── interceptor/           # 攔截器
│   ├── page/                  # 分頁模型
│   ├── redis/                 # Redis工具
│   ├── service/               # 基礎服務接口
│   └── utils/                 # 工具類
└── modules/                   # 業務模塊
    ├── security/              # 安全模塊
    │   ├── config/            # 安全配置
    │   ├── jwt/               # JWT認證
    │   └── oauth2/            # OAuth2認證
    ├── device/                # 設備管理模塊
    │   ├── constant/          # 設備常量
    │   ├── controller/        # 控制器
    │   ├── dao/               # 數據訪問層
    │   ├── dto/               # 數據傳輸對象
    │   ├── entity/            # 設備實體類
    │   ├── service/           # 服務接口
    │   ├── service/impl/      # 服務實現
    │   └── task/              # 定時任務
    └── sys/                   # 系統管理模塊
```

## 主要功能模塊

### 1. 安全認證 (Security)

基於Shiro實現的用戶認證和授權系統：
- 用戶登錄/登出
- 基於Token的認證
- 角色和權限管理
- 驗證碼支持

### 2. 數據權限控制

通過AOP實現的數據過濾機制：
- `@DataFilter` 註解控制數據訪問範圍
- 自動過濾非授權數據
- 支持部門和用戶級別的數據隔離

### 3. 日誌系統

- 操作日誌記錄 (`@LogOperation`)
- 異常日誌捕獲
- Redis緩存日誌

### 4. WebSocket支持

支持實時通信和消息推送

### 5. 設備認證系統

- 設備註冊與管理
- 激活碼生成與驗證
- JWT令牌認證
- 設備狀態管理
- OTA API接口

## 核心類說明

### 基礎組件

- **BaseEntity**: 所有實體類的基類，包含ID、創建者、創建時間等通用字段
- **BaseDao**: 基礎數據訪問接口，繼承MyBatis-Plus的BaseMapper
- **BaseService**: 基礎服務接口，定義通用的CRUD操作

### 數據處理

- **DataScope**: 數據範圍定義類，用於SQL過濾
- **DataFilterInterceptor**: 數據過濾攔截器，攔截SQL並添加數據權限條件
- **FieldMetaObjectHandler**: 字段自動填充處理器，處理創建時間、更新時間等

### 異常處理

- **RenException**: 自定義異常類
- **RenExceptionHandler**: 全局異常處理器
- **ErrorCode**: 錯誤碼定義接口

### 緩存工具

- **RedisUtils**: Redis操作工具類，提供對Redis的鍵值、哈希、列表、集合等操作
  - `set(String key, Object value, long expire)` - 設置緩存值並指定過期時間
  - `get(String key)` - 獲取緩存值
  - `delete(String key)` - 刪除緩存
  - `expire(String key, long expire)` - 設置過期時間
  - `sIsMember(String key, Object value)` - 判斷集合中是否存在元素
  - `sAdd(String key, Object... values)` - 向集合添加元素
  
- **RedisKeys**: Redis鍵定義類，提供系統使用的所有Redis鍵格式
  - `getActivationCodeKey(String code)` - 獲取激活碼緩存鍵
  - `getDeviceActiveTokenKey(Long deviceId)` - 獲取設備當前活躍令牌鍵
  - `getRevokedTokensKey()` - 獲取已撤銷令牌集合鍵

## 設備管理與認證模塊詳解

### 實體類

#### 1. DeviceEntity - 設備實體類

設備信息的數據庫映射類，對應表名：`t_device`

**屬性**:
- `macAddress` - 設備MAC地址
- `clientId` - 設備UUID
- `name` - 設備名稱
- `type` - 設備類型
- `status` - 設備狀態：0-未激活 1-等待激活 2-已激活
- `userId` - 關聯用戶ID
- `updateDate` - 更新時間

#### 2. ActivationCodeEntity - 激活碼實體類

激活碼信息的數據庫映射類，對應表名：`t_activation_code`

**屬性**:
- `code` - 6位數字激活碼
- `deviceId` - 關聯設備ID
- `status` - 狀態：0-有效 1-已使用 2-已過期
- `expireTime` - 過期時間

#### 3. AccessTokenEntity - 訪問令牌實體類

設備訪問令牌的數據庫映射類，對應表名：`t_access_token`

**屬性**:
- `deviceId` - 關聯設備ID
- `token` - JWT令牌
- `isRevoked` - 是否已撤銷：0-否 1-是
- `expireTime` - 過期時間

### 數據傳輸對象(DTO)

#### 1. DeviceDTO - 設備DTO

設備數據傳輸對象，用於前後端交互

**屬性**:
- 包含DeviceEntity的所有屬性
- `createDate` - 創建時間

#### 2. ActivationCodeDTO - 激活碼DTO

激活碼數據傳輸對象，用於前後端交互

**屬性**:
- 包含ActivationCodeEntity的所有屬性
- `device` - 關聯設備信息
- `createDate` - 創建時間

#### 3. AccessTokenDTO - 訪問令牌DTO

訪問令牌數據傳輸對象，用於前後端交互

**屬性**:
- 包含AccessTokenEntity的所有屬性
- `device` - 關聯設備信息
- `createDate` - 創建時間

#### 4. OtaRequestDTO - OTA請求DTO

設備OTA請求數據傳輸對象，用於接收設備OTA請求參數

**屬性**:
- `macAddress` - 設備MAC地址
- `clientId` - 設備UUID
- `deviceType` - 設備類型
- `firmwareVersion` - 固件版本
- `deviceName` - 設備名稱（可選）
- `accessToken` - 訪問令牌（已激活設備可能會提供）

#### 5. OtaResponseDTO - OTA響應DTO

OTA響應數據傳輸對象，根據設備狀態返回不同內容

**屬性**:
- `code` - 響應狀態碼
- `message` - 響應消息
- `deviceStatus` - 設備狀態
- `activationCode` - 激活碼（針對未激活設備）
- `activationExpireTime` - 激活碼過期時間
- `accessToken` - 訪問令牌（針對已激活設備）
- `tokenExpireTime` - 令牌過期時間
- `wsServer` - WebSocket服務器地址
- `wsPort` - WebSocket服務器端口
- `wsPath` - WebSocket路徑

**主要方法**:
- `success()` - 返回成功響應
- `deviceNotFound()` - 返回設備未找到響應
- `deviceInactive(String activationCode, Long expireTime)` - 返回未激活設備響應
- `deviceWaiting(String activationCode, Long expireTime)` - 返回等待激活設備響應
- `deviceActive(String accessToken, Long expireTime, String wsServer, Integer wsPort, String wsPath)` - 返回已激活設備響應
- `error(Integer code, String message)` - 返回錯誤響應

### 配置類

#### 1. OtaConfig - OTA配置類

WebSocket和OTA相關配置

**屬性**:
- `server` - WebSocket服務器地址
- `port` - WebSocket服務器端口
- `path` - WebSocket路徑
- `otaPath` - OTA接口路徑
- `enableDeviceTypeMapping` - 是否啟用設備類型映射

#### 2. OtaConfigAutoConfiguration - OTA配置自動加載類

從application.yml加載OTA相關配置

**內部類**:
- `OtaProperties` - OTA屬性類，使用`@ConfigurationProperties(prefix = "xiaozhi.ws")`從配置文件加載屬性

## 開發環境搭建

### 1. 環境要求

- JDK 21
- Maven 3.8+
- MySQL 8.0+
- Redis

### 2. 創建數據庫

```sql
CREATE DATABASE xiaozhi_esp32_server CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

或者通過Docker安裝MySQL：

```bash
docker run --name xiaozhi-esp32-server-db \
-e MYSQL_ROOT_PASSWORD=123456 \
-p 3306:3306 \
-e MYSQL_DATABASE=xiaozhi_esp32_server \
-e MYSQL_INITDB_ARGS="--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci" \
-d mysql:latest
```

### 3. 配置數據庫連接

編輯 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    username: root
    password: 123456
```

### 4. 啟動應用

```bash
# 開發環境
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

# 生產環境
mvn spring-boot:run "-Dspring-boot.run.profiles=prod"
```

## 項目構建與部署

### 打包

```bash
mvn install
```

### 部署

將生成的JAR包部署到服務器：

```bash
nohup java -jar xiaozhi-esp32-api.jar --spring.profiles.active=dev >/dev/null &
```

## 接口文檔

遵循[manager前後端接口協議](https://app.apifox.com/invite/project?token=H_8qhgfjUeaAL0wybghgU)開發

訪問地址：http://localhost:8002/xiaozhi-esp32-api/doc.html

## 開發規範

- 所有實體類繼承BaseEntity
- 所有DAO接口繼承BaseDao
- 所有Service接口繼承BaseService
- 使用@LogOperation記錄關鍵操作
- 使用@DataFilter控制數據訪問權限

## 項目測試

可以使用以下方式運行測試：

1. Maven測試：`mvn test`
2. 運行單個測試：`mvn test -Dtest=JwtTokenTest`
3. 通過TestRunner類：運行`src/test/java/xiaozhi/TestRunner.java`

## 測試執行指南

> **重要提示**：在運行測試前，請先查閱[測試知識索引](../../TEST-INDEX.md)，了解可能遇到的問題和解決方案。該索引統一收集了所有測試相關資源和經驗教訓。

### 1. 使用Maven運行測試

推薦使用Maven運行所有測試：

```bash
# 運行所有測試
mvn test

# 運行特定測試類
mvn test -Dtest=DeviceTest

# 運行特定測試方法
mvn test -Dtest=DeviceTest#testSmoke
```

### 2. 使用批處理文件運行測試

如果無法使用Maven，可以使用項目根目錄下的`run_tests.bat`批處理文件運行基本測試：

```bash
# 在Windows環境中
cd main/manager-api
.\run_tests.bat
```

批處理文件會自動設置類路徑並運行基本的冒煙測試。

### 3. 測試類說明

項目包含以下測試類：

- `DeviceTest` - 設備服務基本測試
- `ActivationCodeTest` - 激活碼服務基本測試
- `JwtTokenTest` - JWT令牌生成和驗證測試
- `OtaApiTest` - OTA API功能測試（需要Mockito依賴）

### 4. 測試環境配置

測試需要以下環境配置：

- JDK 21
- Maven 3.8+（可選，推薦使用）
- 測試依賴：
  - JUnit Jupiter 5.11.4
  - Mockito（用於OTA API測試）

### 5. 常見問題解決

如果遇到測試相關問題，請參考`learning/manager-api/learning-journal.md`中的以下章節：

- Maven測試配置問題
- Maven測試依賴衝突問題
- JWT令牌測試問題
- Windows環境下測試執行問題
- 測試依賴缺失問題

## 學習資源

有關開發過程中遇到的問題和解決方案，請參考學習日誌：

- [Manager API 學習日誌](../../learning/manager-api/learning-journal.md)

## 最新改進 (2024年6月更新)

### 1. RESTful API標準化

我們對API進行了全面標準化改造，帶來以下優勢：

- **統一的響應格式**: 所有API現在返回標準化的`ApiResponse<T>`對象，包含狀態、消息和數據
- **標準HTTP方法**: 嚴格遵循RESTful原則
  - GET: 用於查詢資源
  - POST: 用於創建資源
  - PUT: 用於修改資源
  - DELETE: 用於刪除資源
- **API版本管理**: 所有新API位於`/api/v1/`路徑下，同時保留舊API以確保向後兼容
- **清晰的錯誤處理**: 標準化的錯誤響應與HTTP狀態碼

#### 新API示例:

```bash
# 獲取激活碼列表
GET /api/v1/activation

# 獲取單個激活碼
GET /api/v1/activation/{id}

# 創建激活碼
POST /api/v1/activation

# 使用激活碼
PUT /api/v1/activation/use

# 刪除激活碼
DELETE /api/v1/activation/{id}
```

### 2. Swagger/Knife4j文檔改進

增強了API文檔系統：

- **JWT認證支持**: 在Swagger UI中添加了Bearer Token認證功能
- **更詳細的API說明**: 完善了操作描述、參數說明和響應示例
- **請求/響應示例**: 添加了請求和響應的示例JSON

### 3. 數據庫與代碼一致性優化

解決了數據庫字段與Java實體類不匹配的問題：

- 統一使用`createDate`和`updateDate`作為實體類屬性名
- 統一使用`create_date`和`update_date`作為數據庫字段名
- 修復了OTA API因字段不匹配導致的調用失敗

### 4. 依賴項更新

更新了主要依賴項版本：

- Spring Boot: 3.4.3
- Mybatis-Plus: 3.5.5
- Knife4j(Swagger): 4.6.0
- Shiro: 2.0.2
- Druid: 1.2.20
- JJWT: 0.12.5

### 5. 主要類和方法更新

#### 新增類

1. **ApiResponse<T>** - 統一API響應格式類
   ```java
   public class ApiResponse<T> {
       private boolean success;  // 是否成功
       private int status;       // HTTP狀態碼
       private String message;   // 消息
       private T data;           // 響應數據
       
       // 靜態工廠方法
       public static <T> ApiResponse<T> success(T data)
       public static <T> ApiResponse<T> success(String message, T data)
       public static <T> ApiResponse<T> error(String message)
       public static <T> ApiResponse<T> error(int status, String message)
   }
   ```

#### 修改類

1. **SwaggerConfig** - 添加JWT令牌支持
   ```java
   @Bean
   public OpenAPI customOpenAPI() {
       // 定義安全模式名稱
       String securitySchemeName = "Bearer Authentication";
       
       return new OpenAPI()
           // ...原有配置...
           .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
           .components(new Components()
               .addSecuritySchemes(securitySchemeName, 
                   new SecurityScheme()
                       .name("Authorization")
                       .type(SecurityScheme.Type.HTTP)
                       .scheme("bearer")
                       .bearerFormat("JWT")
                       .in(SecurityScheme.In.HEADER)
                       .description("請輸入JWT令牌，格式為Bearer {token}")
               )
           );
   }
   ```

2. **DeviceServiceImpl** - 修正字段不匹配問題
   ```java
   // 修改前
   entity.setUpdateTime(new Date());
   // 修改後
   entity.setUpdateDate(new Date());
   
   // 修改前(Lambda表達式)
   .set(DeviceEntity::getUpdateTime, new Date())
   // 修改後(Lambda表達式)
   .set(DeviceEntity::getUpdateDate, new Date())
   ```

3. **ActivationCodeController** - RESTful API改造
   ```java
   // 新API (RESTful風格)
   @GetMapping                         // 獲取列表
   @GetMapping("/{id}")                // 獲取單個
   @PostMapping                        // 創建
   @PutMapping("/use")                 // 修改(使用)
   @DeleteMapping("/{id}")             // 刪除
   @DeleteMapping("/batch")            // 批量刪除
   
   // 舊API (保留但標記為@Deprecated)
   @Deprecated
   @GetMapping("page")                 // 舊版查詢
   @Deprecated
   @PostMapping("generate/{deviceId}") // 舊版創建
   // ...其他舊API
   ```

4. **TokenDTO** - 添加用戶ID字段
   ```java
   @Data
   @Schema(description = "令牌信息")
   public class TokenDTO implements Serializable {
       // 原有字段
       @Schema(description = "密碼")
       private String token;
       
       @Schema(description = "過期時間")
       private int expire;
       
       @Schema(description = "客戶端指紋")
       private String clientHash;
       
       // 新增字段
       @Schema(description = "用戶ID")
       private Long userId;
   }
   ```

5. **Oauth2Filter** - 增強Bearer令牌支持
   ```java
   private String getRequestToken(HttpServletRequest httpRequest) {
       // ...現有代碼...
       
       //如果header中不存在token，則從Authorization頭獲取Bearer token
       if (StringUtils.isBlank(token)) {
           String authHeader = httpRequest.getHeader("Authorization");
           if (!StringUtils.isBlank(authHeader) && authHeader.startsWith("Bearer ")) {
               token = authHeader.substring(7); // 去掉"Bearer "前綴
           }
       }
       
       // ...其餘代碼...
   }
   ```

## 更多詳細信息請參考[學習日誌](../../learning/manager-api/learning-journal.md)

