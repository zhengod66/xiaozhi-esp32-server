# Manager API 學習日誌

> 本文件記錄了Manager API (Java/Spring Boot)開發過程中遇到的問題和解決方案

## 構建與測試問題

### Maven測試配置問題

**問題**: Maven測試被自動跳過，無法執行單元測試

**解決方案**: 
- 檢查pom.xml中的`<skipTests>`設置，改為`false`或移除
- 使用`mvn test -DskipTests=false`臨時覆蓋設置

### Maven依賴問題

**問題**: JUnit Jupiter Engine與Platform版本不兼容，測試無法執行

**解決方案**: 
- 使用JUnit BOM管理依賴版本
- 統一JUnit相關組件版本
- 使用Maven enforcer插件強制版本一致

### JWT令牌測試問題

**問題**: JJWT依賴配置不正確，編譯出錯

**解決方案**: 
- 將`jjwt-impl`和`jjwt-jackson`的scope從`runtime`改為`compile`
- 更新代碼適配JJWT 0.12.5 API (從`setClaims`到`claims`等)

### Windows環境問題

**問題**: Windows環境下無法直接運行Maven命令和測試

**解決方案**: 
- 臨時設置環境變量：
  ```powershell
  $env:MAVEN_HOME="C:\path\to\maven"; $env:PATH="$env:MAVEN_HOME\bin;$env:PATH"; $env:JAVA_HOME="C:\path\to\jdk"
  ```
- 使用批處理文件或直接在IDE中運行

## Spring配置問題

### OTA API配置自動加載問題

**問題**: OTA相關配置無法從application.yml自動加載到OtaConfig類

**解決方案**: 
- 為主應用添加`@EnableConfigurationProperties`注解
- 確保配置類使用正確的`@ConfigurationProperties`注解和前綴

### OTA API安全配置

**問題**: Shiro框架默認阻止了OTA API訪問

**解決方案**: 
- 在ShiroConfig中設置OTA路徑為匿名訪問：
  ```java
  filterMap.put("/xiaozhi/ota/**", "anon")
  ```

### Bean衝突問題

**問題**: Spring啟動失敗，有兩個衝突的OtaProperties Bean

**解決方案**: 
- 在主要的實現類上添加`@Primary`注解
- 或使用`@Qualifier`指定注入的具體Bean

## 數據庫與實體映射問題

**問題描述**: 數據庫表結構與Java實體類不匹配，例如調用不存在的`setUpdateTime`方法

**症狀**: 
- 編譯錯誤，如`The method setUpdateTime(Date) is undefined for the type DeviceEntity`
- `Unknown column 'xxx' in 'field list'`錯誤
- 服務器啟動失敗，日誌顯示`NoSuchMethodError`或`CompilationFailure`

**解決方案**:
1. 分析數據庫結構和實體類定義，確認正確的字段名和方法名
2. 修改不一致的方法調用，例如將`setUpdateTime`改為`setUpdateDate`
3. 在數據庫中添加缺失的列
4. 統一審計欄位命名(creator, create_date, updater, update_date)

**預防措施**:
- 建立和維護統一的數據庫設計文檔
- 制定明確的命名約定
- 避免混合使用`Time`和`Date`後綴
- 考慮使用JPA實體生成工具或Lombok簡化代碼
- 實施代碼審核流程，特別關注數據庫映射一致性
- 使用Liquibase管理數據庫變更

## RESTful API標準化

**問題描述**: 項目API設計不一致，缺乏標準化和版本管理

**症狀**:
- 不同API返回格式不同，處理邏輯不統一
- API命名不規範，有的使用動詞，有的使用名詞
- 同一操作使用不一致的HTTP方法
- 缺少版本管理

**解決方案**:
1. 設計標準響應類`ApiResponse<T>`
2. 重構API遵循RESTful原則:
   - 使用名詞表示資源
   - GET: 查詢資源
   - POST: 創建資源
   - PUT: 修改資源
   - DELETE: 刪除資源
3. 實施API版本管理:
   - 使用路徑前綴`/api/v1/`標識版本
   - 保留舊API並標記為`@Deprecated`
4. 統一HTTP狀態碼使用

**預防措施**:
- 創建API設計規範文檔
- 實施API審核流程
- 使用Swagger/Knife4j生成API文檔
- 自動化測試API一致性

## API文檔編碼問題

**問題描述**: 使用Knife4j和SpringDoc時，API文檔端點返回的不是有效JSON，而是帶引號的Base64編碼字符串

**症狀**:
- 前端控制台顯示錯誤：`SyntaxError: Unexpected token 'e', "eyJvcGVuYX"... is not valid JSON`
- API文檔響應以`"eyJvcGVuYXBpIjo..."`開頭，而不是正常的JSON格式
- API文檔無法正常顯示在Knife4j UI中

**解決方案**:
1. 創建專用的響應包裝過濾器，處理API文檔的特殊編碼問題：
   ```java
   @Component
   @Order(Ordered.HIGHEST_PRECEDENCE)
   public class ApiDocResponseWrapper extends OncePerRequestFilter {
       // 檢測並處理被引號包裹的內容
       if (contentAsString.startsWith("\"") && contentAsString.endsWith("\"")) {
           contentAsString = contentAsString.substring(1, contentAsString.length() - 1)
                   .replace("\\\"", "\"")
                   .replace("\\\\", "\\");
       }
       
       // 檢測並處理Base64編碼的內容
       if (contentAsString.startsWith("eyJ") && isBase64(contentAsString)) {
           String decoded = new String(Base64.getDecoder().decode(contentAsString), StandardCharsets.UTF_8);
           contentAsString = decoded;
       }
       
       // 將內容重新序列化為標準JSON
       Object jsonObj = objectMapper.readValue(contentAsString, Object.class);
       responseWrapper.resetBuffer();
       objectMapper.writeValue(responseWrapper.getWriter(), jsonObj);
   }
   ```

2. 確保正確設置Content-Type和字符編碼：
   ```java
   response.setContentType(MediaType.APPLICATION_JSON_VALUE);
   response.setCharacterEncoding(StandardCharsets.UTF_8.name());
   ```

3. 在application.yml中添加額外的編碼配置：
   ```yaml
   server:
     servlet:
       encoding:
         charset: UTF-8
         enabled: true
         force: true
         force-response: true
     tomcat:
       use-fixed-length-response: true
       uri-encoding: UTF-8
   
   spring:
     http:
       encoding:
         force: true
         force-response: true
         charset: UTF-8
   ```

**預防措施**:
- 使用專門的API文檔調試端點進行問題診斷
- 添加詳細的日誌記錄，特別是處理API文檔請求的部分
- 避免使用自定義序列化/反序列化處理API文檔響應
- 確保所有HTTP過濾器和攔截器正確處理API文檔路徑
- 遵循標準的JSON格式規範
- 將響應包裝和處理邏輯與業務邏輯分離

## 經驗總結

1. **統一命名規範** - 所有表使用相同的審計欄位命名
2. **完整審計信息** - 每個表應包含完整審計欄位
3. **與實體類一致** - 數據庫設計應與實體類結構一致
4. **使用表模板** - 創建標準表模板供新表參考
5. **自動化測試** - 添加測試驗證實體與數據庫映射正確性
6. **依賴管理** - 使用BOM管理依賴版本，避免衝突
7. **環境配置** - 統一開發環境設置，記錄在文檔中
8. **API設計** - 遵循RESTful原則，統一響應格式
9. **文檔維護** - 保持API文檔與實際代碼同步
10. **編碼處理** - 正確處理字符編碼，特別是在API文檔和RESTful響應中

_使用說明: 遇到新問題時，按照問題描述、症狀、解決方案、預防措施的格式添加條目_ 