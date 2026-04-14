# AI-Safety-Platform

基于数字孪生的 AI 模型训推一体可视化平台 & 电力调度 RAG 应用安全研究系统。

## 项目简介

本项目是一个面向电力调度场景的 AI 安全研究平台，包含三大核心子系统：

| 子系统 | 技术栈 | 说明 |
|--------|--------|------|
| **ai-studio** | Spring Boot + Spring Cloud + MySQL + Nacos | 后端微服务（网关、认证、业务） |
| **ai-studio-front** | Vue 3 + Vite + Element Plus + ECharts | 前端单页应用 |
| **rag-server** | FastAPI + ChromaDB + SentenceTransformers | RAG 知识库检索增强生成服务 |

## 整体架构

```
用户浏览器
    │
    ▼ (http://localhost:5173)
┌───────────────────────┐
│   ai-studio-front     │  Vue 3 前端
│   (Vite Dev Server)   │
└───┬───────┬───────┬───┘
    │       │       │
    │/api   │/rag-api│/dashscope-api
    ▼       ▼       ▼
┌───────┐ ┌──────┐ ┌──────────┐
│Gateway│ │ RAG  │ │ 通义千问  │
│ :8080 │ │:8765 │ │ Qwen API │
└───┬───┘ └──────┘ └──────────┘
    │
    ├── auth (:8081)         认证服务
    └── production-line (:8082)  业务服务
            │
    ┌───────┼───────┐
    ▼       ▼       ▼
  MySQL   Redis   Nacos
```

## 环境要求

| 依赖 | 版本要求 |
|------|---------|
| JDK | 11+ |
| Maven | 3.6+ |
| Node.js | 16+ |
| npm | 8+ |
| Python | 3.9+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Nacos | 2.x |

## 快速启动

### 1. 启动基础设施

确保以下服务已运行（可通过 Docker 或本地安装）：

```bash
# MySQL  - 端口 3306，创建数据库 ai_studio，导入 ai-studio/sql/ 下的 SQL 文件
# Redis  - 端口 6379
# Nacos  - 端口 8848，创建命名空间和配置
```

### 2. 启动后端（ai-studio）

```bash
cd ai-studio
mvn clean install -DskipTests

# 依次启动三个服务：
# 1) Gateway    (端口 8080)
# 2) Auth       (端口 8081)
# 3) Production-Line (端口 8082)
```

> 详见 [ai-studio/README.md](./ai-studio/README.md)

### 3. 启动 RAG 服务（rag-server）

```bash
cd rag-server
pip install -r requirements.txt
python main.py
# 服务启动在 http://localhost:8765
```

> 详见 [rag-server/README.md](./rag-server/README.md)

### 4. 启动前端（ai-studio-front）

```bash
cd ai-studio-front
npm install
npm run dev
# 访问 http://localhost:5173
```

> 详见 [ai-studio-front/README.md](./ai-studio-front/README.md)

## 目录结构

```
AI-Safety-Platform/
├── ai-studio/              # Spring Boot 后端微服务
│   ├── gateway/            #   API 网关 (:8080)
│   ├── auth/               #   认证服务 (:8081)
│   ├── production-line/    #   核心业务服务 (:8082)
│   ├── api/                #   公共 API 定义
│   ├── common/             #   公共工具类
│   ├── framework/          #   框架层
│   ├── naic-log-starter/   #   日志组件
│   └── sql/                #   数据库初始化脚本
├── ai-studio-front/        # Vue 3 前端
│   └── src/
│       ├── views/
│       │   ├── ragScheduler/       # RAG 调度模块（知识库/助手/审计）
│       │   ├── securityPlatform/   # 安全攻防验证模块
│       │   ├── dataGovernance/     # 数据治理模块
│       │   ├── digitalTwin/        # 数字孪生模块
│       │   └── ...
│       └── api/                    # API 接口定义
├── rag-server/             # FastAPI RAG 后端
│   ├── main.py             #   API 接口层
│   ├── services.py         #   服务逻辑层
│   ├── models.py           #   数据模型层
│   └── requirements.txt    #   Python 依赖
└── README.md               # 本文件
```

