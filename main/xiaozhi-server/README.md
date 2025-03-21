# Xiaozhi ESP32 Server

基于WebSocket的ESP32设备通信服务器，支持JWT认证机制。

## 功能特性

- 支持基于JWT的WebSocket连接认证
- 使用共享密钥验证设备令牌
- 提供设备连接状态管理和监控
- 支持REST API查询设备状态
- 支持主动断开设备连接

## 安装依赖

```bash
pip install -r requirements.txt
```

## 配置说明

1. **JWT密钥配置**

   复制示例配置文件并修改密钥：
   ```bash
   cp shared-config/jwt-secret.properties.example shared-config/jwt-secret.properties
   # 编辑jwt-secret.properties设置密钥
   ```

2. **服务器配置**

   编辑`config.yaml`文件，添加认证和API配置：
   ```yaml
   auth:
     enabled: true
   
   api_server:
     enabled: true
     host: "0.0.0.0"
     port: 8080
   ```

## 使用方法

### 启动服务器（带认证）

```bash
python app_auth_with_api.py
```

### 连接WebSocket

客户端可以通过两种方式提供JWT令牌：

1. **URL查询参数方式**:
```javascript
// 使用URL查询参数传递JWT令牌
const ws = new WebSocket('ws://your-server:8765/ws?token=eyJhbGciOiJIUzM4NCJ9...');
```

2. **Authorization头部方式**:
```javascript
// 使用Authorization头部传递JWT令牌
const ws = new WebSocket('ws://your-server:8765/ws');
ws.setRequestHeader('Authorization', 'Bearer eyJhbGciOiJIUzM4NCJ9...');

// ESP32设备示例代码
websocket_->SetHeader("Authorization", "Bearer eyJhbGciOiJIUzM4NCJ9...");
```

### 调用API接口

查询所有设备状态：
```bash
curl -H "X-API-Key: admin" http://your-server:8080/api/devices
```

查询特定设备状态：
```bash
curl -H "X-API-Key: admin" http://your-server:8080/api/devices/12345
```

断开设备连接：
```bash
curl -X POST -H "X-API-Key: admin" http://your-server:8080/api/devices/12345/disconnect
```

## 开发说明

### 目录结构

- `core/auth/` - JWT认证相关实现
- `core/connection_manager.py` - 设备连接管理
- `api_server.py` - REST API实现
- `app_auth.py` - 支持JWT认证的服务器入口
- `app_auth_with_api.py` - 同时启动WebSocket和API服务
- `utils/jwt_debug.py` - JWT令牌调试工具（仅用于开发测试）
- `utils/jwt_generate.py` - JWT令牌生成工具（仅用于开发测试）

### JWT认证流程

1. 从WebSocket连接获取令牌（支持两种方式）:
   - 从URL查询参数中提取令牌: `?token=xxx`
   - 从Authorization头部提取令牌: `Authorization: Bearer xxx`
2. 使用共享密钥验证令牌签名
3. 检查令牌的有效性和过期时间
4. 提取设备ID和MAC地址
5. 认证成功后建立连接并跟踪状态

### 开发调试工具

> **重要说明**: 以下工具仅用于开发和测试阶段，不是正常生产环境认证流程的一部分。在生产环境中，设备的JWT令牌应由manager-api生成并存储在数据库中。

#### JWT令牌调试工具

用于解析和验证JWT令牌的内容和有效性：

```bash
# 解析令牌（不验证签名）
python utils/jwt_debug.py eyJhbGciOiJIUzM4NCIsInR5cCI6IkpXVCJ9...

# 验证令牌签名
python utils/jwt_debug.py eyJhbGciOiJIUzM4NCIsInR5cCI6IkpXVCJ9... --verify --config ../../shared-config/jwt-secret.properties
```

#### JWT令牌生成工具

用于开发测试阶段快速生成有效的JWT令牌：

```bash
# 生成默认测试令牌
python utils/jwt_generate.py

# 生成自定义设备令牌
python utils/jwt_generate.py --device-id esp32-dev-01 --mac 11:22:33:44:55:66 --hours 48

# 生成包含WebSocket URL的令牌
python utils/jwt_generate.py --device-id esp32-dev-01 --print-url --server 192.168.1.100:8000
```

## API文档

启动服务器后访问Swagger文档：
```
http://your-server:8080/docs
```

## 更新日志

- **1.0.0** - 实现基本的JWT认证机制和设备连接管理
- **1.0.1** - 添加REST API用于设备状态查询
- **1.0.2** - 增加对Authorization头部JWT令牌的支持

## 许可证

MIT 

## WebSocket通信协议

### 基本消息格式
所有WebSocket消息采用JSON格式，包含`type`字段指示消息类型。

### 初始握手消息
客户端连接后发送hello消息，服务器回复相同格式的hello消息：

**客户端hello消息**:
```json
{
  "type": "hello",
  "version": 1,
  "transport": "websocket",
  "audio_params": {
    "format": "opus",
    "sample_rate": 16000,
    "channels": 1,
    "frame_duration": 20
  }
}
```

**服务器hello响应**:
```json
{
  "type": "hello",
  "transport": "websocket",
  "audio_params": {
    "sample_rate": 16000,
    "format": "opus",
    "channels": 1
  },
  "session_id": "uuid-string"
}
```

### 认证方式
服务器支持以下认证方式：
1. 通过`Authorization`头部传递JWT令牌（标准方式）
2. 通过URL查询参数传递令牌，如`?token=jwt_token_here`

### 其他消息类型
- **abort**: 中止当前操作
- **listen**: 控制客户端监听状态
- **tts**: 文本转语音状态和数据

详细实现见`core/handle`目录下的各个处理器。 