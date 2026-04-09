-- ============================================================
-- 补充建表：knowledge_base, knowledge_document
-- 这两张表在原始脚本中被移除，现在需要补回
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 知识库表 knowledge_base
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base` (
  `id` varchar(50) NOT NULL COMMENT '知识库ID',
  `name` varchar(200) NOT NULL COMMENT '知识库名称',
  `description` varchar(1000) DEFAULT NULL COMMENT '知识库描述',
  `document_count` int DEFAULT 0 COMMENT '文档数量',
  `word_count` bigint DEFAULT 0 COMMENT '总字数',
  `create_user` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG知识库';

INSERT INTO `knowledge_base` (`id`, `name`, `description`, `document_count`, `word_count`, `create_user`, `create_time`, `update_time`) VALUES
('kb_1', '电力安全规程库', '包含国网电力安全工作规程、操作规范等核心文档', 12, 358000, 'admin', '2025-02-15 10:00:00', '2025-03-12 14:30:00'),
('kb_2', '故障诊断知识库', '变电站故障诊断专家经验、典型案例及处置方案', 8, 215000, 'admin', '2025-02-20 09:00:00', '2025-03-11 16:20:00'),
('kb_3', '调度策略知识库', '电力调度操作策略、负荷管理及应急处置知识', 6, 128000, '调度员A', '2025-03-01 08:30:00', '2025-03-10 11:45:00');

-- -----------------------------------------------------------
-- 知识库文档表 knowledge_document
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `knowledge_document`;
CREATE TABLE `knowledge_document` (
  `id` varchar(50) NOT NULL COMMENT '文档ID',
  `knowledge_base_id` varchar(50) NOT NULL COMMENT '所属知识库ID',
  `name` varchar(300) NOT NULL COMMENT '文档名称',
  `size` varchar(30) DEFAULT NULL COMMENT '文件大小',
  `status` varchar(20) DEFAULT 'completed' COMMENT '状态(completed/processing/failed)',
  `chunks` int DEFAULT 0 COMMENT '切片数量',
  `create_user` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_kb_id` (`knowledge_base_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档';

INSERT INTO `knowledge_document` (`id`, `knowledge_base_id`, `name`, `size`, `status`, `chunks`, `create_user`, `create_time`) VALUES
('doc_001', 'kb_1', '电力安全工作规程2024版.pdf', '15.2MB', 'completed', 326, 'admin', '2025-02-15 10:05:00'),
('doc_002', 'kb_1', '变电站操作规范.docx', '8.5MB', 'completed', 185, 'admin', '2025-02-15 10:10:00'),
('doc_003', 'kb_1', '配电线路检修标准.pdf', '12.1MB', 'completed', 264, 'admin', '2025-02-16 09:00:00'),
('doc_004', 'kb_1', '继电保护整定规范.pdf', '6.8MB', 'completed', 148, 'admin', '2025-02-17 14:00:00'),
('doc_005', 'kb_2', '变压器故障诊断手册.pdf', '18.3MB', 'completed', 412, 'admin', '2025-02-20 09:05:00'),
('doc_006', 'kb_2', '开关柜典型故障案例.docx', '5.2MB', 'completed', 98, 'admin', '2025-02-20 09:10:00'),
('doc_007', 'kb_2', '继保装置故障分析.pdf', '9.7MB', 'completed', 215, 'admin', '2025-02-21 10:00:00'),
('doc_008', 'kb_3', '调度操作策略汇编.pdf', '11.4MB', 'completed', 248, '调度员A', '2025-03-01 08:35:00'),
('doc_009', 'kb_3', '负荷管理操作指南.docx', '4.6MB', 'completed', 96, '调度员A', '2025-03-01 08:40:00'),
('doc_010', 'kb_3', '应急处置预案.pdf', '7.8MB', 'completed', 168, '调度员A', '2025-03-02 09:00:00');

SET FOREIGN_KEY_CHECKS = 1;
