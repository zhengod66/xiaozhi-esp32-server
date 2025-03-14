# 项目学习日志

> **注意：此文件已不再更新。請使用新的分類學習日誌：[查看新的學習日誌](/learning/README.md)**

> 记录项目开发过程中遇到的问题和解决方案

## WebSocket连接认证

**问题描述**: 设备无法建立或维持WebSocket连接，服务器日志显示认证失败

**症状**: 
- 连接尝试被拒绝
- 日志中显示401错误
- 设备反复尝试重连

**解决方案**:
- 实现基于JWT的认证机制
- 确保令牌包含设备ID和权限信息
- 添加详细的认证日志

**预防措施**:
- 添加详细的认证日志和监控
- 建立令牌生命周期管理机制
- 定期轮换令牌

## 激活码安全性

**问题描述**: 潜在的激活码猜测攻击风险

**症状**:
- 短时间内大量激活尝试
- 来自单一IP的多次尝试

**解决方案**:
- 增加激活码长度和复杂度
- 添加使用次数限制和IP限制
- 实现激活码速率限制

**预防措施**:
- 实现暴力破解检测机制
- 监控异常激活尝试
- 设置激活码有效期限制

## PowerShell命令限制

**问题描述**: Windows PowerShell中的命令语法与Linux/MacOS终端不同

**症状**:
- PowerShell中不能使用`&&`连接多个命令
- 环境变量引用语法不同
- 路径分隔符可能引起问题

**解决方案**:
- 在PowerShell中使用分号`;`代替`&&`连接命令
- 使用`$env:VARIABLE_NAME`语法引用环境变量
- 对包含空格的路径使用引号包裹

**预防措施**:
- 为Windows环境创建专用批处理文件(.bat)代替Shell脚本
- 记录Windows与其他平台的命令差异
- 考虑使用PowerShell专有的管道和cmdlet语法

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

## Windows與Unix路徑差異

**問題描述**: Windows系統與Unix/Linux/MacOS系統的路徑表示方式不同

**症狀**:
- Windows使用反斜杠`\`，而Unix系統使用正斜杠`/`
- 絕對路徑格式不同（Windows: `C:\path`，Unix: `/path`）
- 使用錯誤格式的路徑導致找不到文件

**解決方案**:
- 在Java代碼中使用`File.separator`獲取系統相容的分隔符
- 使用相對路徑減少跨平台問題
- 在處理路徑字符串時使用`path.replace('\\', '/')`進行標準化

**預防措施**:
- 避免硬編碼絕對路徑
- 使用Java提供的Path API（如`java.nio.file.Paths`）處理路徑
- 在跨平台項目中測試不同系統的路徑處理

## Maven测试依赖冲突问题

### 问题描述
在运行Maven测试时，JUnit Jupiter Engine与JUnit Platform之间存在版本不兼容问题，导致测试无法执行。

### 症状
运行`mvn test`命令时，出现以下错误：
```
NoSuchMethodError: 'org.junit.platform.engine.reporting.OutputDirectoryProvider org.junit.platform.engine.EngineDiscoveryRequest.getOutputDirectoryProvider()'
```

### 解决方案
1. 确保JUnit平台(Platform)、引擎(Engine)和API版本一致，最好使用BOM管理
2. 在pom.xml中添加JUnit BOM依赖管理：
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
3. 可以尝试使用Maven插件强制统一版本:
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

### 预防措施
1. 使用依赖管理工具如Maven BOM管理版本
2. 定期检查项目依赖的健康状况
3. 避免在测试中混用不同版本的JUnit组件
4. 使用`mvn dependency:tree`查看依赖树识别冲突

### 结果分析
应用上述解决方案后，成功运行测试：
- 添加了JUnit BOM依赖管理，统一了JUnit版本为5.11.4
- 移除了各个JUnit依赖的版本号，让它们从BOM继承版本
- 添加了junit-platform-launcher依赖确保测试发现机制正常工作
- 所有2个基础测试（ActivationCodeTest和DeviceTest）都成功运行

测试类都是简单的"冒烟测试"，主要用来验证测试环境是否正常工作。未来需要为项目的核心功能添加更全面的单元测试和集成测试。

_使用說明: 遇到新問題時，按照上述格式添加條目 