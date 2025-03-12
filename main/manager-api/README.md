# 小智ESP32管理后台API

本文档是开发类文档，如需部署小智服务端，[点击这里查看部署教程](../../README.md#%E4%BD%BF%E7%94%A8%E6%96%B9%E5%BC%8F-)

## 项目介绍

小智ESP32管理后台API (manager-api) 是基于SpringBoot框架开发的后台管理系统，提供用户认证、权限管理、数据过滤等功能。系统采用分层架构设计，支持Redis缓存、WebSocket通信等特性。

## 技术栈

- **后端框架**：Spring Boot 3.4.3
- **数据库**：MySQL 8.0+
- **ORM框架**：MyBatis-Plus 3.5.5
- **缓存**：Redis
- **安全框架**：Shiro 2.0.2
- **数据库连接池**：Druid 1.2.20
- **API文档**：Knife4j(Swagger) 4.6.0
- **数据库版本控制**：Liquibase 4.20.0
- **工具库**：Hutool 5.8.24, Lombok
- **前端框架**：Vue 3.x

## 系统架构

项目采用分层架构：
- **表现层**：Controller层处理HTTP请求和响应
- **服务层**：Service层实现业务逻辑
- **数据访问层**：DAO层与数据库交互
- **模型层**：Entity实体类定义数据结构

### 核心包结构

```
src/main/java/xiaozhi/
├── AdminApplication.java      # 应用程序入口
├── common/                    # 公共组件
│   ├── annotation/            # 自定义注解
│   ├── aspect/                # AOP切面
│   ├── config/                # 配置类
│   ├── constant/              # 常量定义
│   ├── convert/               # 数据转换
│   ├── dao/                   # 基础数据访问接口
│   ├── entity/                # 基础实体类
│   ├── exception/             # 异常处理
│   ├── handler/               # 数据处理器
│   ├── interceptor/           # 拦截器
│   ├── page/                  # 分页模型
│   ├── redis/                 # Redis工具
│   ├── service/               # 基础服务接口
│   └── utils/                 # 工具类
└── modules/                   # 业务模块
    ├── security/              # 安全模块
    └── sys/                   # 系统管理模块
```


## 主要功能模块

### 1. 安全认证 (Security)

基于Shiro实现的用户认证和授权系统：
- 用户登录/登出
- 基于Token的认证
- 角色和权限管理
- 验证码支持

### 2. 数据权限控制

通过AOP实现的数据过滤机制：
- `@DataFilter` 注解控制数据访问范围
- 自动过滤非授权数据
- 支持部门和用户级别的数据隔离

### 3. 日志系统

- 操作日志记录 (`@LogOperation`)
- 异常日志捕获
- Redis缓存日志

### 4. WebSocket支持

支持实时通信和消息推送

## 核心类说明

### 基础组件

- **BaseEntity**: 所有实体类的基类，包含ID、创建者、创建时间等通用字段
- **BaseDao**: 基础数据访问接口，继承MyBatis-Plus的BaseMapper
- **BaseService**: 基础服务接口，定义通用的CRUD操作

### 数据处理

- **DataScope**: 数据范围定义类，用于SQL过滤
- **DataFilterInterceptor**: 数据过滤拦截器，拦截SQL并添加数据权限条件
- **FieldMetaObjectHandler**: 字段自动填充处理器，处理创建时间、更新时间等

### 异常处理

- **RenException**: 自定义异常类
- **RenExceptionHandler**: 全局异常处理器
- **ErrorCode**: 错误码定义接口

### 缓存工具

- **RedisUtils**: Redis操作工具类
- **RedisKeys**: Redis键定义类

## 开发环境搭建

### 1. 环境要求

- JDK 21
- Maven 3.8+
- MySQL 8.0+
- Redis

### 2. 创建数据库

```sql
CREATE DATABASE xiaozhi_esp32_server CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

或者通过Docker安装MySQL：

```bash
docker run --name xiaozhi-esp32-server-db \
-e MYSQL_ROOT_PASSWORD=123456 \
-p 3306:3306 \
-e MYSQL_DATABASE=xiaozhi_esp32_server \
-e MYSQL_INITDB_ARGS="--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci" \
-d mysql:latest
```

### 3. 配置数据库连接

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    username: root
    password: 123456
```

### 4. 启动应用

运行 `src/main/java/xiaozhi/AdminApplication.java` 的 `main` 方法

### 5. 访问API文档

启动后访问：http://localhost:8002/xiaozhi-esp32-api/doc.html

## 项目构建与部署

### 打包

```bash
mvn install
```

### 部署

将生成的JAR包部署到服务器：

```bash
nohup java -jar xiaozhi-esp32-api.jar --spring.profiles.active=dev >/dev/null &
```

## 接口文档

遵循[manager前后端接口协议](https://app.apifox.com/invite/project?token=H_8qhgfjUeaAL0wybghgU)开发

访问地址：http://localhost:8002/xiaozhi-esp32-api/doc.html

## 开发规范

- 所有实体类继承BaseEntity
- 所有DAO接口继承BaseDao
- 所有Service接口继承BaseService
- 使用@LogOperation记录关键操作
- 使用@DataFilter控制数据访问权限

