# 通用學習記錄

> 本文件記錄了跨項目的通用問題和解決方案

## PowerShell命令限制

**問題描述**: Windows PowerShell中的命令語法與Linux/MacOS終端不同

**症狀**:
- PowerShell中不能使用`&&`連接多個命令
- 環境變量引用語法不同
- 路徑分隔符可能引起問題

**解決方案**:
- 在PowerShell中使用分號`;`代替`&&`連接命令
- 使用`$env:VARIABLE_NAME`語法引用環境變量
- 對包含空格的路徑使用引號包裹

**預防措施**:
- 為Windows環境創建專用批處理文件(.bat)代替Shell腳本
- 記錄Windows與其他平台的命令差異
- 考慮使用PowerShell專有的管道和cmdlet語法

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

_使用說明: 遇到新問題時，按照上述格式添加條目_ 