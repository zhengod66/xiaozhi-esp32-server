# 小智ESP32設備認證系統 - 測試知識索引

> 本文檔整合了項目測試過程中積累的知識和經驗，為測試活動提供指導。

## 1. 常見測試問題

### 1.1 數據庫結構與實體類不匹配

**問題描述**:  
實體類與數據庫表結構不一致，導致SQL錯誤或數據存取失敗。

**症狀**:
- SQL異常：`Unknown column 'xxx' in 'field list'`
- 字段映射錯誤
- 數據保存失敗但無明顯錯誤日誌

**解決方案**:
- 檢查實體類與數據庫表結構的映射關係
- 使用Liquibase修復腳本添加/重命名缺失的列
- 調整實體類的注解或命名以匹配數據庫

**驗證方法**:
1. 使用以下SQL查詢數據庫表結構：
```sql
DESCRIBE table_name;
```

2. 與實體類中的字段進行對比，確保：
   - 所有非`@Transient`字段在表中有對應的列
   - 命名方式一致（考慮駝峰式與下劃線的轉換）
   - 數據類型匹配

3. 特別檢查審計欄位：
   - `creator`
   - `create_date`
   - `updater`
   - `update_date`

### 1.2 測試數據准備不足

**問題描述**:  
缺少適當的測試數據，導致測試覆蓋不全面或測試場景不完整。

**症狀**:
- 測試只覆蓋"正常路徑"
- 邊界條件和錯誤處理未被測試
- 測試數據重用導致的假陽性結果

**解決方案**:
- 創建標準化的測試數據集
- 使用數據庫種子腳本
- 實現自動化測試數據生成

## 2. 自動化測試指南

### 2.1 單元測試

單元測試專注於測試最小可測試單元（通常是方法級別）。

**測試框架**:
- JUnit 5
- AssertJ

**最佳實踐**:
- 測試方法應遵循AAA模式：Arrange, Act, Assert
- 測試方法名應清晰描述測試場景
- 使用`@DisplayName`提高測試可讀性

**示例**:
```java
@Test
@DisplayName("設備ID從令牌中正確提取")
void shouldExtractDeviceIdFromToken() {
    // Arrange
    String token = tokenProvider.generateToken(1L, "00:11:22:33:44:55");
    
    // Act
    Long deviceId = tokenProvider.getDeviceIdFromToken(token);
    
    // Assert
    assertThat(deviceId).isEqualTo(1L);
}
```

### 2.2 集成測試

集成測試關注於多個組件之間的交互。

**測試範圍**:
- 控制器與服務層集成
- 數據庫訪問
- 外部服務集成

**測試工具**:
- Spring Test
- TestContainers (數據庫集成)

## 3. 特定功能測試指南

### 3.1 設備認證流程測試

**測試場景**:
1. 未註冊設備初次請求
2. 等待激活的設備請求
3. 已激活設備請求
4. 令牌過期場景
5. 令牌吊銷場景

**關鍵斷言點**:
- 設備狀態轉換正確
- 激活碼生成和驗證
- 令牌生成和驗證

### 3.2 WebSocket連接測試

**測試場景**:
1. 認證成功建立連接
2. 認證失敗拒絕連接
3. 連接中斷自動重連
4. 消息傳輸可靠性

## 4. 測試環境配置

### 4.1 本地測試環境

**數據庫配置**:
- 使用H2內存數據庫
- 啟用Liquibase自動建表

**Redis配置**:
- 使用內嵌Redis服務器

### 4.2 測試腳本使用指南

運行位於`main/manager-api`目錄的`run_tests.bat`：
```
cd main/manager-api
.\run_tests.bat
```

腳本將自動設置類路徑並運行基本冒煙測試。

## 5. 故障排除

### 5.1 實體-表映射檢查工具

當懷疑實體類與數據表結構不匹配時，可以使用以下檢查方法：

1. 創建以下測試類：
```java
@SpringBootTest
class EntityMappingTest {
    
    @Autowired
    private EntityManager entityManager;
    
    @ParameterizedTest
    @MethodSource("provideEntityClasses")
    void testEntityMappingToDatabase(Class<?> entityClass) {
        // 獲取實體的元數據
        EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);
        String tableName = entityClass.getAnnotation(Table.class).name();
        
        // 獲取表結構
        Query query = entityManager.createNativeQuery(
            "DESCRIBE " + tableName);
        List<Object[]> tableColumns = query.getResultList();
        
        // 比較字段和列...
        // 實現詳細比較邏輯
    }
    
    static Stream<Class<?>> provideEntityClasses() {
        return Stream.of(
            DeviceEntity.class,
            ActivationCodeEntity.class,
            AccessTokenEntity.class
        );
    }
}
```

2. 或者使用以下SQL腳本檢查特定表的審計欄位：
```sql
SELECT 
  table_name, 
  MAX(CASE WHEN column_name = 'creator' THEN 'Y' ELSE 'N' END) has_creator,
  MAX(CASE WHEN column_name = 'create_date' THEN 'Y' ELSE 'N' END) has_create_date,
  MAX(CASE WHEN column_name = 'updater' THEN 'Y' ELSE 'N' END) has_updater,
  MAX(CASE WHEN column_name = 'update_date' THEN 'Y' ELSE 'N' END) has_update_date
FROM information_schema.columns
WHERE table_schema = 'your_database_name'
GROUP BY table_name;
```

## 6. 參考資源

- [JUnit 5 用戶指南](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 文檔](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [TestContainers 指南](https://www.testcontainers.org/) 