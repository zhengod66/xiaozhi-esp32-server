本文档是开发类文档，如需部署小智服务端，[点击这里查看部署教程](../README.md#%E4%BD%BF%E7%94%A8%E6%96%B9%E5%BC%8F-)

# 项目目录介绍
当你看到这份文件的时候，这个这个项目还没完善好。我们还有很多东西要做。

如果你会开发，我们非常欢迎您的加入。

# 小智ESP32智能语音助手服务端架构

## 项目总体结构
xiaozhi-esp32-server/
├─ xiaozhi-server      # 8000端口 Python语言开发，基于FastAPI框架，负责与ESP32设备通信
├─ manager-web         # 8001端口 Node.js+Vue开发，提供控制台web界面
├─ manager-api         # 8002端口 Java语言开发，基于Spring Boot，提供控制台API接口

## 一、xiaozhi-server (Python) 核心组件

### 1. 通信模块
- WebSocket服务: 基于FastAPI实现与ESP32设备的全双工实时通信
  - 方法: handle_websocket_connection - 处理WebSocket连接
  - 方法: process_audio_frame - 处理设备传来的音频帧
  - 方法: send_audio_response - 向设备发送音频响应

### 2. 语音处理模块
- VAD (SileroVAD): 语音活动检测
  - 类: SileroVAD
    - 方法: detect_speech - 检测音频中的语音片段
    - 方法: is_speech - 判断当前帧是否为语音
    - 配置: vad.threshold - 语音检测阈值

- ASR (FunASR): 语音识别
  - 类: FunASR
    - 方法: recognize - 将语音转换为文本
    - 方法: transcribe - 转录完整音频
  - 类: VolcEngineASR - 火山引擎语音识别
  - 配置: asr.provider, asr.model

### 3. 对话生成模块
- LLM提供者:
  - 抽象类: LLMProviderBase
    - 方法: generate() - 文本生成
    - 方法: stream_generate() - 流式文本生成
  - 类: ChatGLMProvider - 智谱AI
  - 类: AliLLM - 阿里百炼
  - 类: DeepSeekLLM - 深度求索
  - 类: OllamaLLM - 本地部署
  - 类: DifyLLM - Dify平台
  - 类: GeminiLLM - Google Gemini
  - 类: CozeLLM - Coze平台
  - 类: HomeAssistantLLM - 家庭助手集成
  - 配置: llm.provider, llm.api_key, llm.url

### 4. 语音合成模块
- TTS提供者:
  - 抽象类: TTSProviderBase
    - 方法: synthesize - 文本转语音
    - 方法: stream_synthesize - 流式文本转语音
  - 类: EdgeTTSProvider - Microsoft边缘TTS
  - 类: DoubaoTTSProvider - 火山引擎豆包TTS
  - 类: CosyVoiceSiliconflow - Siliconflow语音
  - 类: CozeCnTTS - Coze中文TTS
  - 类: FishSpeech - 本地TTS服务
  - 类: GPT_SOVITS_V2 - 本地个性化语音合成
  - 配置: tts.provider, tts.voice

### 5. 会话管理
- 类: ConversationManager
  - 方法: process_audio - 处理音频输入
  - 方法: generate_reply - 生成回复
  - 方法: handle_wake_word - 处理唤醒词
  - 功能: 管理VAD→ASR→LLM→TTS的端到端流程

### 6. 配置管理
- 文件: data/config.yaml
  - 服务器配置: host, port, auth
  - 设备配置: session_timeout
  - LLM配置: provider, api_key, url
  - TTS配置: provider, voice
  - ASR配置: provider, model
  - VAD配置: threshold, min_silence

## 二、manager-api (Java) 核心组件

### 1. 应用入口
- 类: AdminApplication
  - 方法: main - 应用程序入口点，启动Spring Boot应用

### 2. 基础架构
- 数据访问:
  - 接口: BaseDao<T> - 继承MyBatis-Plus的BaseMapper，提供基础数据访问
  - 类: BaseEntity - 所有实体类的基类，包含id、creator、createDate等通用字段
  - 类: FieldMetaObjectHandler - 处理实体字段自动填充

- 服务接口:
  - 接口: BaseService<T> - 所有Service接口的基类
    - 方法: insert - 插入记录
    - 方法: updateById - 根据ID更新
    - 方法: selectById - 根据ID查询
    - 方法: deleteById - 根据ID删除

- 安全模块:
  - 类: SecurityUser - 当前用户信息处理
  - 类: UserDetail - 用户详情
  - 注解: @DataFilter - 数据权限过滤注解
  - 类: DataFilterAspect - 数据权限AOP实现
  - 类: DataFilterInterceptor - SQL拦截器，实现数据权限

### 3. 常用工具
- Redis缓存:
  - 类: RedisUtils
    - 方法: set - 设置缓存
    - 方法: get - 获取缓存
    - 方法: delete - 删除缓存
  - 类: RedisKeys - Redis键常量
  - 类: RedisConfig - Redis配置

- 异常处理:
  - 接口: ErrorCode - 错误码定义
  - 类: RenException - 自定义异常
  - 类: RenExceptionHandler - 全局异常处理器

- 数据分页:
  - 类: PageData<T> - 分页数据封装

- 配置:
  - 类: MybatisPlusConfig - MyBatis-Plus配置
  - 类: SwaggerConfig - Swagger API文档配置

### 4. 日志审计
- 注解: @LogOperation - 操作日志注解

### 5. 数据库
- 使用MySQL 8.0+
- 使用Liquibase进行数据库版本管理
- 使用MyBatis-Plus作为ORM框架

## 三、manager-web (Node.js+Vue) 组件

### 1. 前端框架
- Vue 3.x - 前端MVVM框架
- 界面参考: https://codesign.qq.com/app/s/526108506410828

### 2. API通信
- 基于manager-api提供的接口进行通信
- 接口文档: https://app.apifox.com/invite/project?token=H_8qhgfjUeaAL0wybghgU

## 四、跨组件交互

### 1. xiaozhi-server与ESP32设备
- 通信协议: WebSocket
- 协议文档: https://ccnphfhqs21z.feishu.cn/wiki/M0XiwldO9iJwHikpXD5cEx71nKh
- 音频格式: Opus编码

### 2. manager-web与manager-api
- 通信协议: HTTP RESTful API
- 请求格式: JSON
- 认证方式: Token鉴权

### 3. manager-api与xiaozhi-server
- 设备状态同步
- 配置管理

## 五、部署和运行

### xiaozhi-server
- Docker部署: docker-compose up -d
- 本地部署: uvicorn app:app --host 0.0.0.0 --port 8000

### manager-api
- 打包: mvn install
- 运行: java -jar xiaozhi-esp32-api.jar --spring.profiles.active=dev

### manager-web
- 构建: npm run build
- 部署: 将dist目录部署到web服务器

## 六、待开发功能

1. 会话记忆功能
2. 多种情绪模式
3. 智能控制面板Web UI
