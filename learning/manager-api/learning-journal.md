# Manager API 學習日誌

> 本文件記錄了Manager API (Java/Spring Boot)開發過程中遇到的問題和解決方案

## Maven測試配置問題

**問題描述**: Maven測試被自動跳過，無法執行單元測試

**症狀**: 
- 運行`mvn test`時顯示"Tests are skipped"
- 編譯通過但測試未運行
- 無測試報告生成

**解決方案**:
- 檢查pom.xml中的測試配置，找到並修改`<skipTests>true</skipTests>`設置
- 可以改為`<skipTests>false</skipTests>`或完全移除此配置
- 也可以在命令行使用`mvn test -DskipTests=false`臨時覆蓋此設置

**預防措施**:
- 在新項目初始化時檢查測試配置
- 建立持續集成流程確保測試總是被執行
- 定期審查pom.xml確保測試配置正確

## Maven测试依赖冲突问题

**問題描述**: 在運行Maven測試時，JUnit Jupiter Engine與JUnit Platform之間存在版本不兼容問題，導致測試無法執行。

**症狀**:
運行`mvn test`命令時，出現以下錯誤：
```
NoSuchMethodError: 'org.junit.platform.engine.reporting.OutputDirectoryProvider org.junit.platform.engine.EngineDiscoveryRequest.getOutputDirectoryProvider()'
```

**解決方案**:
1. 確保JUnit平台(Platform)、引擎(Engine)和API版本一致，最好使用BOM管理
2. 在pom.xml中添加JUnit BOM依賴管理：
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.junit</groupId>
            <artifactId>junit-bom</artifactId>
            <version>5.12.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
3. 可以嘗試使用Maven插件強制統一版本:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <executions>
        <execution>
            <id>enforce-versions</id>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <dependencyConvergence />
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**預防措施**:
1. 使用依賴管理工具如Maven BOM管理版本
2. 定期檢查項目依賴的健康狀況
3. 避免在測試中混用不同版本的JUnit組件
4. 使用`mvn dependency:tree`查看依賴樹識別衝突

**結果分析**:
應用上述解決方案後，成功運行測試：
- 添加了JUnit BOM依賴管理，統一了JUnit版本為5.11.4
- 移除了各個JUnit依賴的版本號，讓它們從BOM繼承版本
- 添加了junit-platform-launcher依賴確保測試發現機制正常工作
- 所有2個基礎測試（ActivationCodeTest和DeviceTest）都成功運行

測試類都是簡單的"冒煙測試"，主要用來驗證測試環境是否正常工作。未來需要為項目的核心功能添加更全面的單元測試和集成測試。

_使用說明: 遇到新問題時，按照上述格式添加條目_ 