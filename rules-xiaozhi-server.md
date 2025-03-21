# Xiaozhi Server (Python) 开发规则

## 异步FastAPI
- 利用FastAPI的异步功能处理WebSocket
- 避免事件循环中的阻塞调用
- 使用worker线程处理CPU密集型任务
- 返回有意义的HTTP状态码和WebSocket关闭代码

## 代码风格
- 遵循PEP 8规范
- 为所有函数添加类型提示
- 使用Pydantic模型验证请求/响应
- 使用Black格式化代码

## WebSocket安全
- 实现基于JWT的认证机制
- 限制连接频率防止DoS攻击
- 在连接中验证设备ID和权限
- 监控和记录连接状态变化

## AI模型执行
- 在应用启动时加载模型
- 缓存频繁使用的输出
- 监控内存/GPU使用情况
- 实现优雅降级机制

## 学习笔记
<!-- 项目中遇到的问题和解决方案记录在这里 -->
- **问题**: [描述问题]
- **解决方案**: [解决方法] 