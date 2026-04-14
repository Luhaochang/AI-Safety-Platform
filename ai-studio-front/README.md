# ai-studio-front — 前端应用

基于 Vue 3 + Vite + Element Plus 的单页前端应用。

## 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4 | 前端框架 |
| Vite | 6.3 | 构建工具 & 开发服务器 |
| Element Plus | 2.7 | UI 组件库 |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.4 | 路由 |
| Axios | 1.11 | HTTP 请求 |
| ECharts | 5.5 | 数据可视化图表 |
| TailwindCSS | 3.4 | 样式工具 |
| Three.js | 0.183 | 3D 数字孪生可视化 |
| Ant Design Vue | 4.x | 辅助 UI 组件 |

## 环境要求

- **Node.js** >= 16
- **npm** >= 8

## 安装 & 启动

```bash
# 1. 进入前端目录
cd ai-studio-front

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 4. 浏览器访问
# http://localhost:5173
```

## 构建生产版本

```bash
# 构建
npm run build:prod

# 预览构建结果
npm run preview
```

构建产物输出到 `dist/` 目录。

## 代理配置

开发模式下，Vite 会将 API 请求代理到对应后端服务：

| 前端路径 | 代理目标 | 对应服务 |
|----------|----------|----------|
| `/api/*` | `http://localhost:8080` | Spring Cloud Gateway |
| `/rag-api/*` | `http://localhost:8765` | RAG Server (FastAPI) |
| `/dashscope-api/*` | `https://dashscope.aliyuncs.com` | 通义千问 Qwen API |

代理规则定义在 `vite.config.js` 的 `server.proxy` 中，如需修改后端地址请编辑该文件。

## 通义千问 API Key 配置

调度助手模块的大模型对话功能依赖通义千问 API，配置方式：

1. 前往 [DashScope 控制台](https://dashscope.console.aliyun.com/) 获取 API Key
2. 编辑 `.env.development` 文件：

```env
VITE_QWEN_API_KEY = sk-your-api-key-here
```

> 如果不填写 API Key，调度助手将使用 Mock 数据进行演示。

## 功能模块一览

| 模块 | 路由 | 说明 |
|------|------|------|
| RAG 知识库管理 | `/ragScheduler/knowledge` | 知识库创建、文档上传、分块向量化 |
| RAG 调度助手 | `/ragScheduler/chat` | 基于知识库的智能问答对话 |
| RAG 决策审计 | `/ragScheduler/decisionLog` | 问答日志查询、导出 |
| 数据治理 | `/dataGovernance/*` | 数据分级分类、资产管理、血缘追踪 |
| 数字孪生 | `/digitalTwin/*` | 资源状态可视化、3D 场景 |
| 安全平台-环境管理 | `/securityPlatform/envManage` | RAG 环境装载与资源监控 |
| 安全平台-攻击验证 | `/securityPlatform/attackVerify` | 知识库投毒攻击模拟 |
| 安全平台-防御配置 | `/securityPlatform/defenseConfig` | 输入/检索/输出护栏策略 |
| 安全平台-态势感知 | `/securityPlatform/situationAware` | 攻击链关联与风险评估 |
| 安全平台-评测复盘 | `/securityPlatform/evaluation` | 指标分析与过程回放 |

## 目录结构

```
ai-studio-front/
├── src/
│   ├── api/                    # 后端 API 接口定义
│   │   ├── ragScheduler/       #   RAG 相关接口
│   │   ├── dataGovernance/     #   数据治理接口
│   │   ├── digitalTwin/        #   数字孪生接口
│   │   └── ...
│   ├── views/                  # 页面组件
│   │   ├── ragScheduler/       #   知识库 / 助手 / 审计
│   │   ├── securityPlatform/   #   安全攻防五大模块
│   │   ├── dataGovernance/     #   数据治理
│   │   ├── digitalTwin/        #   数字孪生
│   │   └── ...
│   ├── store/                  # Pinia 状态管理
│   ├── router/                 # 路由配置
│   ├── components/             # 公共组件
│   └── assets/                 # 静态资源
├── public/                     # 公共资源
├── vite.config.js              # Vite 配置（含代理规则）
├── .env.development            # 开发环境变量
├── .env.production             # 生产环境变量
├── package.json                # 依赖清单
└── tailwind.config.js          # TailwindCSS 配置
```

## 常见问题

**Q: `npm install` 很慢或失败？**
A: 使用国内镜像：`npm install --registry=https://registry.npmmirror.com`

**Q: 启动后页面白屏或 API 报 502？**
A: 检查后端服务（Gateway :8080）是否已启动。RAG 相关功能需要 rag-server (:8765) 也启动。

**Q: RAG 知识库功能不可用？**
A: 需要先启动 `rag-server`（端口 8765），详见 [rag-server/README.md](../rag-server/README.md)。未启动时界面会自动降级为 Mock 数据。
