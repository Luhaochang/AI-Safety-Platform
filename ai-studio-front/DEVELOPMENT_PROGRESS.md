# AI训推一体数字孪生平台 — 开发进度

## 已完成的前端模块

### 模块一：数据分层分级管理

| 文件 | 说明 |
|---|---|
| `src/api/dataGovernance/asset.js` | 数据资产管理API（含Mock） |
| `src/api/dataGovernance/classification.js` | 分级分类API + 血缘数据API（含Mock） |
| `src/views/dataGovernance/dashboard.vue` | 资产看板：统计卡片 + 等级分布饼图 + 层级柱状图 + 趋势折线图 + 操作时间线 |
| `src/views/dataGovernance/assetCatalog.vue` | 资产目录：搜索筛选 + 资产表格 + 新增/编辑对话框 + 详情抽屉 |
| `src/views/dataGovernance/classification.vue` | 分级配置：4级安全等级卡片 + 颜色标识 + 访问策略 + 新增/编辑 |
| `src/views/dataGovernance/lineage.vue` | 数据血缘：D3.js血缘DAG图 + 缩放/平移 + 节点详情侧栏 + 层级/等级筛选 |

### 模块二：训推可视化（增强）

| 文件 | 说明 |
|---|---|
| `src/api/modelMag/trainVis.js` | 训练指标/推理性能/实验对比API（含Mock） |
| `src/views/model/trainVis/index.vue` | 三合一Tab页面：训练监控(Loss/Acc/GPU) + 推理分析(QPS/延迟/P99) + 实验对比表格 |

### 模块三：数字孪生可视化

| 文件 | 说明 |
|---|---|
| `src/api/digitalTwin/index.js` | 集群实时状态/告警/任务流转API（含Mock） |
| `src/views/digitalTwin/index.vue` | 暗色主题态势感知大屏：集群概要6卡片 + 3x2节点拓扑卡片(CPU/GPU/内存进度条) + 数据流转 + ECharts仪表盘 + 告警面板，5秒自动刷新 |

### 模块四：RAG调度决策

| 文件 | 说明 |
|---|---|
| `src/api/ragScheduler/index.js` | 对话/知识库/文档/决策日志API（含Mock，预留Dify对接接口） |
| `src/views/ragScheduler/chat.vue` | 对话式调度助手：建议卡片 + 消息流(Markdown渲染) + RAG引用来源展示 + 历史对话列表 |
| `src/views/ragScheduler/knowledge.vue` | 知识库管理：知识库列表 + 文档列表 + 上传文档 + 统计信息 |
| `src/views/ragScheduler/decisionLog.vue` | 决策审计日志：搜索 + 日志表格 + 详情抽屉(问题/回答/引用) |

### 路由集成

| 路由路径 | 侧边栏菜单 |
|---|---|
| `/data-governance/dashboard` | 数据治理 → 资产看板 |
| `/data-governance/asset-catalog` | 数据治理 → 资产目录 |
| `/data-governance/classification` | 数据治理 → 分级配置 |
| `/data-governance/lineage` | 数据治理 → 数据血缘 |
| `/digital-twin` | 数字孪生 |
| `/train-vis` | 训推可视化 |
| `/rag-scheduler/chat` | RAG调度决策 → 调度助手 |
| `/rag-scheduler/knowledge` | RAG调度决策 → 知识库管理 |
| `/rag-scheduler/decision-log` | RAG调度决策 → 决策日志 |

## 技术说明

- 所有API均提供Mock实现，可在无后端情况下完整运行
- 使用现有技术栈：Vue 3 + Element Plus + Ant Design Vue + ECharts + D3.js + TailwindCSS
- 路由已添加到 `src/router/index.js` 的 `constantRoutes` 中，无需后端菜单配置即可显示
- 数字孪生当前为2D拓扑视图，安装TresJS后可升级为3D场景

## 后续开发事项

1. **后端API实现**：在`ai-studio`的`production-line`模块中实现对应的Controller/Service/Mapper
2. **Dify部署**：`docker compose`部署Dify + Ollama，替换RAG模块的Mock数据
3. **3D升级**：安装`@tresjs/core` + `three`，将数字孪生升级为3D机房场景
4. **权限对接**：将新模块菜单录入数据库，通过RBAC权限体系控制访问
5. **WebSocket**：数字孪生和训练监控页面接入WebSocket实现真正实时推送
