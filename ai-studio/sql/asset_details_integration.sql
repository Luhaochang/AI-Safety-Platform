-- ============================================================
-- 资产明细功能整合说明
-- 整合内容：资产目录 + 资产看板 => 资产明细
-- 日期：2025-03-17
-- ============================================================

-- 说明：
-- 本次功能整合将"资产目录"和"资产看板"两个独立功能合并为统一的"资产明细"功能。
-- 前端通过标签页切换实现目录表格视图和可视化看板视图的统一展示。
-- 
-- 数据库层面无需修改：
-- 1. data_asset 表已包含所有必要字段（资产名称、分级、分类、层级、来源等）
-- 2. data_grade 表提供分级维度数据（公开级、重要级、核心级）
-- 3. data_category 表提供分类维度数据（7大类）
-- 4. 现有表结构完全支持目录查询和统计分析需求
--
-- 后端API保持不变：
-- - GET /production-line/data-asset/condition - 分页查询资产列表（目录视图）
-- - GET /production-line/data-asset/overview - 获取统计概览（看板视图）
-- - POST /production-line/data-asset - 新增资产
-- - PUT /production-line/data-asset - 修改资产
-- - DELETE /production-line/data-asset - 删除资产
-- - GET /production-line/data-asset/by-id - 根据ID查询资产详情
--
-- 前端路由变更：
-- 原路由：
--   /datamag/asset-catalog (资产目录)
--   /datamag/dashboard (资产看板)
-- 新路由：
--   /datamag/asset-details (资产明细 - 统一入口)
--
-- 功能特性：
-- 1. 目录视图：表格展示所有资产，支持搜索、筛选、分页
-- 2. 看板视图：可视化图表展示资产分布、分级分类统计、操作趋势
-- 3. 统一的顶部统计卡片：资产总数、数据集、模型、知识文档
-- 4. 标签页切换：用户可在目录和看板视图间自由切换
--
-- ============================================================
-- 以下为现有表结构参考（无需执行，仅供文档说明）
-- ============================================================

/*
-- 数据资产表（核心表）
CREATE TABLE `data_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asset_name` varchar(200) NOT NULL COMMENT '资产名称',
  `grade_id` bigint DEFAULT NULL COMMENT '分级ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `data_layer` varchar(50) DEFAULT NULL COMMENT '数据层级',
  `source` varchar(50) DEFAULT NULL COMMENT '来源',
  `owner` varchar(64) DEFAULT NULL COMMENT '负责人',
  `size` varchar(30) DEFAULT NULL COMMENT '数据大小',
  `description` varchar(1000) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` varchar(64) DEFAULT NULL,
  `update_user` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_grade` (`grade_id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据资产表';

-- 数据分级表
CREATE TABLE `data_grade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分级名称(公开级/重要级/核心级)',
  `sort` int DEFAULT 0,
  `description` varchar(500) DEFAULT NULL,
  `access_policy` varchar(500) DEFAULT NULL,
  `examples` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分级表';

-- 数据分类表
CREATE TABLE `data_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(500) DEFAULT NULL,
  `examples` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分类表';
*/

-- ============================================================
-- 验证查询示例
-- ============================================================

-- 查询资产总数
-- SELECT COUNT(*) AS total_assets FROM data_asset;

-- 按分级统计
-- SELECT g.name, COUNT(a.id) AS count 
-- FROM data_grade g 
-- LEFT JOIN data_asset a ON g.id = a.grade_id 
-- GROUP BY g.id, g.name;

-- 按分类统计
-- SELECT c.name, COUNT(a.id) AS count 
-- FROM data_category c 
-- LEFT JOIN data_asset a ON c.id = a.category_id 
-- GROUP BY c.id, c.name;

-- 按数据层级统计
-- SELECT data_layer, COUNT(*) AS count 
-- FROM data_asset 
-- WHERE data_layer IS NOT NULL 
-- GROUP BY data_layer;

-- ============================================================
-- 结论：数据库无需任何修改，现有结构完全满足整合需求
-- ============================================================
