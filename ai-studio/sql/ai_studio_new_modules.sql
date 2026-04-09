-- ============================================================
-- AI Studio 新模块建表脚本（修订版）
-- 保留8张表：data_grade, data_category, data_asset,
--   data_lineage_node, data_lineage_edge, decision_log, cluster_node
-- 已移除：knowledge_base, knowledge_document, system_alert
-- 数据库: MySQL 8.x
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 1. 数据分级表 data_grade （去掉code、color）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `data_grade`;
CREATE TABLE `data_grade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分级名称(公开级/重要级/核心级)',
  `sort` int DEFAULT 0 COMMENT '排序',
  `description` varchar(500) DEFAULT NULL COMMENT '分级说明',
  `access_policy` varchar(500) DEFAULT NULL COMMENT '访问策略描述',
  `examples` varchar(500) DEFAULT NULL COMMENT '典型数据举例',
  `create_user` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分级表';

INSERT INTO `data_grade` (`id`, `name`, `sort`, `description`, `access_policy`, `examples`) VALUES
(1, '公开级', 1, '可公开使用的一般性数据，泄露不会对电力系统运行造成影响', '所有平台用户可访问', '公开技术文档、设备型号参数、已脱敏的统计报表'),
(2, '重要级', 2, '涉及电力系统运行细节的数据，泄露可能导致局部运行风险', '经审批的内部人员可访问，需记录访问日志', '设备运行参数、调度指令记录、模型训练数据集'),
(3, '核心级', 3, '电力系统核心敏感数据，泄露可能危及电网安全或国家安全', '仅限授权管理员访问，需双人复核', '电网拓扑结构、调度决策模型、安全防护策略、用户隐私数据');

-- -----------------------------------------------------------
-- 2. 数据分类表 data_category （去掉code、icon、color、default_grade_id）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `data_category`;
CREATE TABLE `data_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(500) DEFAULT NULL COMMENT '分类说明',
  `examples` varchar(500) DEFAULT NULL COMMENT '典型数据举例',
  `create_user` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分类表';

INSERT INTO `data_category` (`id`, `name`, `description`, `examples`) VALUES
(1, '设备数据类', '电力设备的基础信息、运行参数、健康状态等结构化数据', '变压器参数、开关状态、传感器读数、设备台账'),
(2, '运行数据类', '电力系统实时运行产生的时序数据和状态数据', '电压/电流/功率时序、负荷曲线、故障录波、PMU数据'),
(3, '调度指令类', '电力调度操作中产生的指令、操作票和执行记录', '调度操作票、倒闸操作指令、负荷调整指令、事故处置指令'),
(4, '历史案例类', '历史故障案例、处置经验和事故分析报告', '故障分析报告、事故处置记录、典型缺陷案例、抢修记录'),
(5, '专家知识类', '领域专家的经验知识、决策规则和判断依据', '故障诊断规则、运维经验总结、调度策略知识、安全规程解读'),
(6, '规程文档类', '电力行业标准、操作规程、管理制度等规范性文档', '电力安全规程、调度管理规程、设备检修标准、应急预案'),
(7, '用户信息类', '涉及用户个人信息、用电行为等隐私相关数据', '用户用电数据、个人身份信息、缴费记录、投诉工单');

-- -----------------------------------------------------------
-- 3. 数据资产表 data_asset （增加create_time，去掉dataset_id）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `data_asset`;
CREATE TABLE `data_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asset_name` varchar(200) NOT NULL COMMENT '资产名称',
  `grade_id` bigint DEFAULT NULL COMMENT '分级ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `data_layer` varchar(50) DEFAULT NULL COMMENT '数据层级(原始数据/清洗数据/标注数据/特征数据/模型产物/知识文档)',
  `source` varchar(50) DEFAULT NULL COMMENT '来源(本地上传/SCADA采集/训练产出/推理产出/爬虫采集/人工录入)',
  `owner` varchar(64) DEFAULT NULL COMMENT '负责人',
  `size` varchar(30) DEFAULT NULL COMMENT '数据大小',
  `description` varchar(1000) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(64) DEFAULT NULL,
  `update_user` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_grade` (`grade_id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据资产表';

INSERT INTO `data_asset` (`asset_name`, `grade_id`, `category_id`, `data_layer`, `source`, `owner`, `size`, `description`) VALUES
('变压器监测数据集', 2, 1, '原始数据', 'SCADA采集', 'admin', '1280MB', '220kV主变各测点实时采集数据'),
('电网负荷时序数据', 2, 2, '原始数据', 'SCADA采集', 'admin', '3200MB', '全网各节点负荷5分钟采样数据'),
('调度操作票记录', 3, 3, '原始数据', '人工录入', '调度员A', '450MB', '2024年度全量调度操作票'),
('220kV线路故障案例', 1, 4, '知识文档', '人工录入', '运维员B', '120MB', '近五年220kV线路故障分析汇编'),
('故障诊断专家规则', 2, 5, '知识文档', '人工录入', '调度员A', '85MB', '变电站故障诊断专家经验规则库'),
('电力安全操作规程', 1, 6, '知识文档', '本地上传', 'admin', '200MB', '国网电力安全工作规程2024版'),
('用户用电行为数据', 3, 7, '原始数据', 'SCADA采集', 'admin', '980MB', '工商业用户日用电负荷特征'),
('开关柜状态数据集', 2, 1, '清洗数据', 'SCADA采集', '运维员B', '560MB', '10kV开关柜运行状态清洗后数据'),
('继电保护动作数据', 2, 2, '清洗数据', 'SCADA采集', 'admin', '780MB', '继电保护动作信号及时序清洗'),
('负荷预测训练数据', 2, 2, '标注数据', '训练产出', '调度员A', '1500MB', '已标注的负荷预测模型训练数据集'),
('故障标注数据集-v2', 3, 2, '标注数据', '训练产出', '调度员A', '2100MB', '故障类型标注数据集第二版'),
('PMU量测数据集', 2, 2, '原始数据', 'SCADA采集', 'admin', '4200MB', '同步相量测量装置原始数据'),
('调度策略知识文档', 2, 5, '知识文档', '人工录入', '调度员A', '95MB', '调度策略与负荷管理知识汇编'),
('设备检修标准文档', 1, 6, '知识文档', '本地上传', 'admin', '180MB', '变电设备检修工艺标准'),
('配电网拓扑数据', 3, 1, '特征数据', '本地上传', 'admin', '350MB', '10kV配电网拓扑结构及参数'),
('风电出力预测数据', 2, 2, '特征数据', 'SCADA采集', '运维员B', '890MB', '风电场出力及气象特征数据'),
('事故处置经验库', 1, 4, '知识文档', '人工录入', '调度员A', '65MB', '典型事故处置经验案例库'),
('电能质量监测数据', 2, 2, '原始数据', 'SCADA采集', 'admin', '1100MB', '电能质量在线监测数据集'),
('BERT-故障分类模型', 3, 5, '模型产物', '训练产出', 'admin', '420MB', 'BERT微调的故障文本分类模型'),
('YOLOv8-设备检测模型', 3, 1, '模型产物', '训练产出', '运维员B', '280MB', 'YOLOv8设备外观缺陷检测模型');

-- -----------------------------------------------------------
-- 4. 数据血缘节点表 data_lineage_node （layer改为varchar，去掉create_time）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `data_lineage_node`;
CREATE TABLE `data_lineage_node` (
  `id` varchar(50) NOT NULL COMMENT '节点ID',
  `name` varchar(200) NOT NULL COMMENT '节点名称',
  `type` varchar(50) DEFAULT NULL COMMENT '类型(设备数据类/运行数据类/模型产物/推理服务/业务应用等)',
  `layer` varchar(50) DEFAULT NULL COMMENT '层级(原始数据/清洗数据/标注数据/特征数据/模型产物/推理服务)',
  `grade` varchar(30) DEFAULT NULL COMMENT '数据分级(公开级/重要级/核心级)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据血缘节点';

INSERT INTO `data_lineage_node` (`id`, `name`, `type`, `layer`, `grade`) VALUES
('dev_1', '变压器监测数据', '设备数据类', '原始数据', '重要级'),
('op_1', '电网运行时序数据', '运行数据类', '原始数据', '重要级'),
('doc_1', '电力安全规程', '规程文档类', '原始数据', '公开级'),
('case_1', '历史故障案例库', '历史案例类', '原始数据', '公开级'),
('clean_1', '清洗后设备数据集', '设备数据类', '清洗数据', '重要级'),
('clean_2', '清洗后运行数据集', '运行数据类', '清洗数据', '重要级'),
('label_1', '故障标注数据集', '运行数据类', '标注数据', '核心级'),
('kb_1', '调度知识库(RAG)', '专家知识类', '标注数据', '重要级'),
('model_1', '故障诊断模型', '模型产物', '模型产物', '核心级'),
('model_2', '负荷预测模型', '模型产物', '模型产物', '核心级'),
('svc_1', '故障诊断推理服务', '推理服务', '推理服务', '核心级'),
('svc_2', '负荷预测推理服务', '推理服务', '推理服务', '核心级'),
('app_1', '电力调度决策应用', '业务应用', '业务应用', '核心级');

-- -----------------------------------------------------------
-- 5. 数据血缘边表 data_lineage_edge （去掉create_time）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `data_lineage_edge`;
CREATE TABLE `data_lineage_edge` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_id` varchar(50) NOT NULL COMMENT '源节点ID',
  `target_id` varchar(50) NOT NULL COMMENT '目标节点ID',
  `label` varchar(100) DEFAULT NULL COMMENT '边标签(数据清洗/模型训练等)',
  PRIMARY KEY (`id`),
  KEY `idx_source` (`source_id`),
  KEY `idx_target` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据血缘边';

INSERT INTO `data_lineage_edge` (`source_id`, `target_id`, `label`) VALUES
('dev_1', 'clean_1', '数据清洗'),
('op_1', 'clean_2', '数据清洗'),
('clean_1', 'label_1', '故障标注'),
('clean_2', 'label_1', '特征关联'),
('doc_1', 'kb_1', '知识抽取'),
('case_1', 'kb_1', '案例入库'),
('label_1', 'model_1', '模型训练'),
('clean_2', 'model_2', '模型训练'),
('model_1', 'svc_1', '服务部署'),
('model_2', 'svc_2', '服务部署'),
('svc_1', 'app_1', '诊断服务'),
('svc_2', 'app_1', '预测服务'),
('kb_1', 'app_1', 'RAG检索');

-- -----------------------------------------------------------
-- 6. RAG决策审计日志表 decision_log （去掉conversation_id）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `decision_log`;
CREATE TABLE `decision_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question` varchar(2000) NOT NULL COMMENT '用户提问',
  `answer` text COMMENT 'AI回答',
  `references_count` int DEFAULT 0 COMMENT '引用文档数',
  `user` varchar(64) DEFAULT NULL COMMENT '用户',
  `duration` varchar(20) DEFAULT NULL COMMENT '响应耗时',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG决策审计日志';

INSERT INTO `decision_log` (`question`, `answer`, `references_count`, `user`, `duration`, `create_time`) VALUES
('220kV宝华线发生单相接地故障，如何进行故障隔离与恢复供电？', '隔离故障线路两侧开关，合联络线转移负荷，组织巡线查明故障点后恢复供电', 3, '值班调度员', '2.4s', '2025-03-12 10:28:00'),
('主变压器过负荷运行，请给出紧急限负荷调度方案', '合母联将负荷转移至相邻主变，按限电序位切除三类非重要负荷，顶层油温不超95°C', 3, '值班调度员', '1.9s', '2025-03-11 15:18:00'),
('系统频率持续偏低0.2Hz，如何进行一次调频和二次调频操作？', '通知水电厂增加出力实施一次调频，启动抽水蓄能发电工况，AGC系统执行二次调频', 3, '系统运行员', '2.2s', '2025-03-10 09:42:00'),
('节假日用电高峰期如何制定负荷调度计划与备用方案？', '预置热备用容量不低于最大负荷的15%，启动需求响应预案，与大工业用户签订调峰协议', 2, '计划调度员', '1.7s', '2025-03-09 13:55:00'),
('500kV变电站母线保护动作跳闸，应如何隔离和恢复送电？', '确认母线保护正确动作后隔离故障母线，切换至旁路母线运行，逐步恢复各出线送电', 4, '值班调度员', '3.1s', '2025-03-08 11:25:00');

-- -----------------------------------------------------------
-- 7. 集群节点表 cluster_node （去掉role、create_time、update_time）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `cluster_node`;
CREATE TABLE `cluster_node` (
  `id` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL COMMENT '节点名称',
  `ip` varchar(50) NOT NULL COMMENT 'IP地址',
  `status` varchar(20) DEFAULT 'running' COMMENT '状态(running/warning/offline)',
  `cpu_cores` int DEFAULT 0,
  `gpu_count` int DEFAULT 0,
  `gpu_model` varchar(50) DEFAULT NULL,
  `memory_total` int DEFAULT 0 COMMENT '内存总量(GB)',
  `disk_total` int DEFAULT 0 COMMENT '磁盘总量(GB)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集群节点';

INSERT INTO `cluster_node` (`id`, `name`, `ip`, `status`, `cpu_cores`, `gpu_count`, `gpu_model`, `memory_total`, `disk_total`) VALUES
('node-01', 'Master-01', '10.33.38.21', 'running', 64, 4, 'A100-80G', 256, 4000),
('node-02', 'Worker-01', '10.33.38.22', 'running', 64, 4, 'A100-80G', 256, 4000),
('node-03', 'Worker-02', '10.33.38.23', 'running', 32, 2, 'V100-32G', 128, 2000),
('node-04', 'Worker-03', '10.33.38.24', 'warning', 64, 4, 'A100-80G', 256, 4000),
('node-05', 'Inference-01', '10.33.38.25', 'running', 32, 2, 'T4-16G', 64, 1000),
('node-06', 'Inference-02', '10.33.38.26', 'offline', 32, 2, 'T4-16G', 64, 1000);

SET FOREIGN_KEY_CHECKS = 1;
