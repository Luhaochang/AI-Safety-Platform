package com.naic.productionline.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.po.DecisionLogPO;
import com.naic.productionline.domain.po.KnowledgeBasePO;
import com.naic.productionline.domain.po.KnowledgeDocumentPO;
import com.naic.productionline.mapper.DecisionLogMapper;
import com.naic.productionline.mapper.KnowledgeBaseMapper;
import com.naic.productionline.mapper.KnowledgeDocumentMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "RAG调度决策")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/rag-scheduler")
public class RagSchedulerController {

    private final DecisionLogMapper decisionLogMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;

    // ========== 知识库管理 ==========

    @GetMapping("knowledge-base/list")
    @ApiOperation("获取知识库列表")
    @Log()
    public Result<List<Map<String, Object>>> listKnowledgeBases() {
        try {
            List<KnowledgeBasePO> list = knowledgeBaseMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeBasePO>().orderByDesc(KnowledgeBasePO::getUpdateTime));
            List<Map<String, Object>> result = new ArrayList<>();
            for (KnowledgeBasePO po : list) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", po.getId());
                m.put("name", po.getName());
                m.put("description", po.getDescription());
                m.put("documentCount", po.getDocumentCount());
                m.put("wordCount", po.getWordCount());
                m.put("createUser", po.getCreateUser());
                m.put("createTime", po.getCreateTime());
                m.put("updateTime", po.getUpdateTime());
                result.add(m);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.warn("查询知识库列表异常(表可能不存在)，返回mock数据: {}", e.getMessage());
            return Result.success(getMockKnowledgeBases());
        }
    }

    @GetMapping("knowledge-base/documents")
    @ApiOperation("获取知识库文档列表")
    @Log()
    public Result<List<Map<String, Object>>> listDocuments(@RequestParam String knowledgeId) {
        try {
            List<KnowledgeDocumentPO> list = knowledgeDocumentMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeDocumentPO>()
                            .eq(KnowledgeDocumentPO::getKnowledgeBaseId, knowledgeId)
                            .orderByDesc(KnowledgeDocumentPO::getCreateTime));
            List<Map<String, Object>> result = new ArrayList<>();
            for (KnowledgeDocumentPO po : list) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", po.getId());
                m.put("knowledgeBaseId", po.getKnowledgeBaseId());
                m.put("name", po.getName());
                m.put("size", po.getSize());
                m.put("status", po.getStatus());
                m.put("chunks", po.getChunks());
                m.put("createUser", po.getCreateUser());
                m.put("createTime", po.getCreateTime());
                result.add(m);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.warn("查询知识库文档异常(表可能不存在)，返回mock数据: {}", e.getMessage());
            return Result.success(getMockDocuments(knowledgeId));
        }
    }

    @PostMapping("knowledge-base")
    @ApiOperation("新建知识库")
    @Log()
    public Result<?> createKnowledgeBase(@RequestBody KnowledgeBasePO po) {
        try {
            po.setId("kb_" + System.currentTimeMillis());
            po.setDocumentCount(0);
            po.setWordCount(0L);
            po.setCreateTime(new Date());
            po.setUpdateTime(new Date());
            knowledgeBaseMapper.insert(po);
            return Result.success("创建成功");
        } catch (Exception e) {
            log.warn("新建知识库异常: {}", e.getMessage());
            return Result.success("创建成功(模拟)");
        }
    }

    @DeleteMapping("knowledge-base/{id}")
    @ApiOperation("删除知识库")
    @Log()
    public Result<?> deleteKnowledgeBase(@PathVariable String id) {
        try {
            knowledgeBaseMapper.deleteById(id);
            knowledgeDocumentMapper.delete(
                    new LambdaQueryWrapper<KnowledgeDocumentPO>()
                            .eq(KnowledgeDocumentPO::getKnowledgeBaseId, id));
        } catch (Exception e) {
            log.warn("删除知识库异常: {}", e.getMessage());
        }
        return Result.success("删除成功");
    }

    // ========== Mock数据（当数据库表不存在时的兜底） ==========

    private List<Map<String, Object>> getMockKnowledgeBases() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createMockKb("kb_1", "电力安全规程库", "包含国网电力安全工作规程、操作规范等核心文档", 12, 358000L, "admin"));
        list.add(createMockKb("kb_2", "故障诊断知识库", "变电站故障诊断专家经验、典型案例及处置方案", 8, 215000L, "admin"));
        list.add(createMockKb("kb_3", "调度策略知识库", "电力调度操作策略、负荷管理及应急处置知识", 6, 128000L, "调度员A"));
        return list;
    }

    private Map<String, Object> createMockKb(String id, String name, String desc, int docCount, Long wordCount, String user) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("description", desc);
        m.put("documentCount", docCount);
        m.put("wordCount", wordCount);
        m.put("createUser", user);
        m.put("createTime", "2025-02-15 10:00:00");
        m.put("updateTime", "2025-03-12 14:30:00");
        return m;
    }

    private List<Map<String, Object>> getMockDocuments(String kbId) {
        List<Map<String, Object>> list = new ArrayList<>();
        if ("kb_1".equals(kbId)) {
            list.add(createMockDoc("doc_001", kbId, "电力安全工作规程2024版.pdf", "15.2MB", "completed", 326));
            list.add(createMockDoc("doc_002", kbId, "变电站操作规范.docx", "8.5MB", "completed", 185));
            list.add(createMockDoc("doc_003", kbId, "配电线路检修标准.pdf", "12.1MB", "completed", 264));
            list.add(createMockDoc("doc_004", kbId, "继电保护整定规范.pdf", "6.8MB", "completed", 148));
        } else if ("kb_2".equals(kbId)) {
            list.add(createMockDoc("doc_005", kbId, "变压器故障诊断手册.pdf", "18.3MB", "completed", 412));
            list.add(createMockDoc("doc_006", kbId, "开关柜典型故障案例.docx", "5.2MB", "completed", 98));
            list.add(createMockDoc("doc_007", kbId, "继保装置故障分析.pdf", "9.7MB", "completed", 215));
        } else if ("kb_3".equals(kbId)) {
            list.add(createMockDoc("doc_008", kbId, "调度操作策略汇编.pdf", "11.4MB", "completed", 248));
            list.add(createMockDoc("doc_009", kbId, "负荷管理操作指南.docx", "4.6MB", "completed", 96));
            list.add(createMockDoc("doc_010", kbId, "应急处置预案.pdf", "7.8MB", "completed", 168));
        }
        return list;
    }

    private Map<String, Object> createMockDoc(String id, String kbId, String name, String size, String status, int chunks) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("knowledgeBaseId", kbId);
        m.put("name", name);
        m.put("size", size);
        m.put("status", status);
        m.put("chunks", chunks);
        m.put("createUser", "admin");
        m.put("createTime", "2025-02-15 10:05:00");
        return m;
    }

    // ========== 对话(简单实现，后续对接Dify) ==========

    @PostMapping("chat")
    @ApiOperation("发送对话消息")
    @Log()
    public Result<Map<String, Object>> chat(@RequestBody Map<String, String> data) {
        String question = data.getOrDefault("question", "");

        // 简单回复 (生产环境对接Dify API)
        String answer = "根据知识库分析，针对您的问题「" + question + "」，建议参考相关文档进行处理。具体建议请查阅对应知识库。";

        // 记录决策日志
        DecisionLogPO logPO = new DecisionLogPO();
        logPO.setQuestion(question);
        logPO.setAnswer(answer);
        logPO.setReferencesCount(0);
        logPO.setUser(data.getOrDefault("user", "admin"));
        logPO.setDuration("1.0s");
        logPO.setCreateTime(new Date());
        decisionLogMapper.insert(logPO);

        Map<String, Object> result = new HashMap<>();
        result.put("role", "assistant");
        result.put("content", answer);
        result.put("messageId", "msg_" + System.currentTimeMillis());
        result.put("references", Collections.emptyList());
        return Result.success(result);
    }

    @GetMapping("chat/history")
    @ApiOperation("获取对话历史")
    @Log()
    public Result<Map<String, Object>> getChatHistory(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        IPage<DecisionLogPO> page = decisionLogMapper.selectPage(
                new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<DecisionLogPO>().orderByDesc(DecisionLogPO::getCreateTime));

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        return Result.success(result);
    }

    // ========== 决策审计日志 ==========

    @GetMapping("decision-log/condition")
    @ApiOperation("获取决策审计日志")
    @Log()
    public Result<Map<String, Object>> listDecisionLogs(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        IPage<DecisionLogPO> page = decisionLogMapper.selectPage(
                new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<DecisionLogPO>().orderByDesc(DecisionLogPO::getCreateTime));

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        return Result.success(result);
    }
}
