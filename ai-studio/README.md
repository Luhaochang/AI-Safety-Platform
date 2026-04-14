# ai-studio — 后端微服务

基于 Spring Boot 2.3 + Spring Cloud Hoxton + Spring Cloud Alibaba 的微服务后端。

## 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.3.2.RELEASE | 基础框架 |
| Spring Cloud | Hoxton.SR10 | 微服务治理 |
| Spring Cloud Alibaba | 2.2.6.RELEASE | Nacos 注册/配置中心 |
| MyBatis-Plus | 3.5.2 | ORM 持久层 |
| MySQL | 8.0.20 | 关系数据库 |
| Redis | 6.0+ | 缓存 & 会话 |
| Druid | 1.2.23 | 数据库连接池 |
| Knife4j | 3.0.2 | API 文档 |
| MinIO | — | 对象存储 |
| JDK | 11 | 编译 & 运行 |

## 模块结构

```
ai-studio/
├── gateway/            API 网关（端口 8080）
│                       路由转发、跨域、鉴权拦截
├── auth/               认证服务（端口 8081）
│                       登录、注册、Token 管理
├── production-line/    核心业务服务（端口 8082）
│                       数据治理、模型管理、RAG调度、数字孪生、安全平台
├── api/                公共 API 定义 & Feign 接口
├── common/             公共工具类 & 常量
├── framework/          框架层（安全、异常处理、MyBatis配置）
├── naic-log-starter/   自定义日志注解组件
└── sql/                数据库初始化脚本
```

## 前置依赖

在启动后端之前，请确保以下基础设施已经运行：

### 1. MySQL

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE ai_studio DEFAULT CHARACTER SET utf8mb4;"

# 导入初始化脚本（按顺序执行）
mysql -u root -p ai_studio < sql/ai_studio_new_modules.sql
mysql -u root -p ai_studio < sql/add_knowledge_tables.sql
mysql -u root -p ai_studio < sql/asset_details_integration.sql
```

默认连接配置：
- 地址：`localhost:3306`
- 数据库：`ai_studio`
- 用户名：`root`

> 如需修改连接信息，编辑各模块的 `application.yml` 中 `spring.datasource` 配置。

### 2. Redis

```bash
# 默认配置
# 地址：localhost:6379
```

### 3. Nacos

```bash
# 默认配置
# 地址：localhost:8848
# 命名空间组：AI-STUDIO
# 共享配置文件：ai-studio-config-dev.yml
```

需要在 Nacos 控制台中创建分组 `AI-STUDIO`，并添加配置文件 `ai-studio-config-dev.yml`。

> Nacos 地址可在各模块的 `bootstrap.yaml` 中修改。

### 4. MinIO（可选）

```bash
# 默认配置
# 地址：http://localhost:9000
# 用户名：minioadmin
# 密码：minioadmin123
# Bucket：ai-studio
```

## 编译 & 启动

```bash
# 1. 在根目录编译所有模块
cd ai-studio
mvn clean install -DskipTests

# 2. 启动 Gateway（端口 8080）
cd gateway
mvn spring-boot:run
# 或: java -jar target/gateway-1.0.0.jar

# 3. 启动 Auth（端口 8081）
cd ../auth
mvn spring-boot:run

# 4. 启动 Production-Line（端口 8082）
cd ../production-line
mvn spring-boot:run
```

> **启动顺序**：建议先启动 Nacos → Gateway → Auth → Production-Line

## 端口汇总

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 8080 | API 网关入口，前端所有 `/api` 请求经此转发 |
| Auth | 8081 | 认证鉴权服务 |
| Production-Line | 8082 | 核心业务服务 |

## API 文档

启动后访问各服务的 Knife4j 文档：

- Auth: `http://localhost:8081/doc.html`
- Production-Line: `http://localhost:8082/doc.html`

## 常见问题

**Q: 编译报错 `java.lang.module` 相关错误？**
A: 确保使用 JDK 11，项目已在 pom.xml 中配置了必要的 `--add-opens` 参数。

**Q: 启动时连接 Nacos 失败？**
A: 检查 Nacos 是否已启动，并确认 `bootstrap.yaml` 中的 `server-addr` 配置正确。

**Q: 前端请求 404？**
A: 确保 Gateway 已启动，前端 `/api` 代理指向 `localhost:8080`。
