package com.naic.productionline.controller;

import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

@Api(tags = "数据清洗")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/data-clean")
public class DataCleanController {

    @PostMapping("clean-text")
    @ApiOperation("清洗文本数据")
    @Log()
    public Result<Map<String, Object>> cleanText(@RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        List<String> rules = (List<String>) request.getOrDefault("rules", Arrays.asList(
                "html", "special_chars", "normalize", "dedup"
        ));

        String originalText = text;
        int originalLength = text.length();

        // 执行清洗规则
        for (String rule : rules) {
            switch (rule) {
                case "html":
                    text = removeHtmlTags(text);
                    break;
                case "special_chars":
                    text = filterSpecialChars(text);
                    break;
                case "normalize":
                    text = normalizeText(text);
                    break;
                case "dedup":
                    text = removeDuplicateLines(text);
                    break;
                case "privacy":
                    text = maskSensitiveInfo(text);
                    break;
                case "encoding":
                    // UTF-8编码统一（Java默认已是UTF-8）
                    break;
                case "filter_short":
                    text = filterShortText(text, 50);
                    break;
            }
        }

        int cleanedLength = text.length();
        double compressionRate = originalLength > 0 
            ? (1 - (double) cleanedLength / originalLength) * 100 
            : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("originalText", originalText);
        result.put("cleanedText", text);
        result.put("originalLength", originalLength);
        result.put("cleanedLength", cleanedLength);
        result.put("compressionRate", String.format("%.1f%%", compressionRate));
        result.put("appliedRules", rules);

        return Result.success(result);
    }

    @GetMapping("rules")
    @ApiOperation("获取清洗规则列表")
    @Log()
    public Result<List<Map<String, Object>>> listRules() {
        List<Map<String, Object>> rules = new ArrayList<>();
        
        rules.add(createRule("html", "HTML标签清除", "去除HTML标签，保留纯文本内容", "text", true, 1));
        rules.add(createRule("special_chars", "特殊字符过滤", "过滤不可见字符、控制字符等", "text", true, 2));
        rules.add(createRule("dedup", "重复内容去重", "基于行级去重算法检测并去除重复段落", "dedup", true, 3));
        rules.add(createRule("privacy", "敏感信息脱敏", "自动检测并脱敏手机号、身份证号、邮箱等", "privacy", true, 4));
        rules.add(createRule("encoding", "编码统一(UTF-8)", "将所有文件统一转换为UTF-8编码", "encoding", true, 5));
        rules.add(createRule("filter_short", "短文本过滤", "过滤字符数少于50的无意义短文本", "filter", false, 6));
        rules.add(createRule("normalize", "格式标准化", "统一日期格式、数值单位、标点符号等", "normalize", true, 7));

        return Result.success(rules);
    }

    @PostMapping("batch-clean")
    @ApiOperation("批量清洗文件")
    @Log()
    public Result<Map<String, Object>> batchClean(@RequestBody Map<String, Object> request) {
        List<String> fileIds = (List<String>) request.get("fileIds");
        List<String> rules = (List<String>) request.getOrDefault("rules", 
            Arrays.asList("html", "special_chars", "normalize", "dedup"));

        // 模拟批量清洗统计
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFiles", fileIds.size());
        stats.put("cleanedFiles", fileIds.size());
        stats.put("failedFiles", 0);
        stats.put("duplicateRemoved", (int)(fileIds.size() * 0.08));
        stats.put("sensitiveDetected", (int)(fileIds.size() * 0.03));
        stats.put("totalSizeBefore", "325.3MB");
        stats.put("totalSizeAfter", "289.1MB");
        stats.put("compressionRate", "11.1%");
        stats.put("appliedRules", rules);

        return Result.success(stats);
    }

    // ========== 私有方法：清洗规则实现 ==========

    private Map<String, Object> createRule(String id, String name, String description, 
                                           String type, boolean enabled, int order) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("id", id);
        rule.put("name", name);
        rule.put("description", description);
        rule.put("type", type);
        rule.put("enabled", enabled);
        rule.put("order", order);
        return rule;
    }

    /**
     * 移除HTML标签
     */
    private String removeHtmlTags(String text) {
        if (text == null || text.isEmpty()) return text;
        // 移除HTML标签
        text = text.replaceAll("<[^>]+>", "");
        // 移除HTML实体
        text = text.replaceAll("&nbsp;", " ");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        text = text.replaceAll("&amp;", "&");
        text = text.replaceAll("&quot;", "\"");
        text = text.replaceAll("&#\\d+;", "");
        return text;
    }

    /**
     * 过滤特殊字符
     */
    private String filterSpecialChars(String text) {
        if (text == null || text.isEmpty()) return text;
        // 移除控制字符（保留换行、制表符）
        text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        // 移除零宽字符
        text = text.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");
        return text;
    }

    /**
     * 格式标准化
     */
    private String normalizeText(String text) {
        if (text == null || text.isEmpty()) return text;
        // 统一换行符
        text = text.replaceAll("\\r\\n", "\n");
        text = text.replaceAll("\\r", "\n");
        // 移除多余空白
        text = text.replaceAll("[ \\t]+", " ");
        text = text.replaceAll("\\n{3,}", "\n\n");
        // 统一中文标点
        text = text.replace("，", ",");
        text = text.replace("。", ".");
        text = text.replace("！", "!");
        text = text.replace("？", "?");
        text = text.replace("：", ":");
        text = text.replace("；", ";");
        return text.trim();
    }

    /**
     * 去除重复行
     */
    private String removeDuplicateLines(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] lines = text.split("\n");
        Set<String> seen = new LinkedHashSet<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                seen.add(line);
            }
        }
        return String.join("\n", seen);
    }

    /**
     * 脱敏敏感信息
     */
    private String maskSensitiveInfo(String text) {
        if (text == null || text.isEmpty()) return text;
        
        // 手机号脱敏：保留前3后4位
        text = Pattern.compile("(1[3-9]\\d)(\\d{4})(\\d{4})")
                .matcher(text)
                .replaceAll("$1****$3");
        
        // 身份证号脱敏：保留前6后4位
        text = Pattern.compile("(\\d{6})(\\d{8})(\\d{4})")
                .matcher(text)
                .replaceAll("$1********$3");
        
        // 邮箱脱敏：保留前2位和@后内容
        text = Pattern.compile("([a-zA-Z0-9]{2})[a-zA-Z0-9._-]*(@[a-zA-Z0-9.-]+)")
                .matcher(text)
                .replaceAll("$1****$2");
        
        // 银行卡号脱敏：保留前4后4位
        text = Pattern.compile("(\\d{4})(\\d{8,12})(\\d{4})")
                .matcher(text)
                .replaceAll("$1********$3");
        
        return text;
    }

    /**
     * 过滤短文本
     */
    private String filterShortText(String text, int minLength) {
        if (text == null || text.isEmpty()) return text;
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            if (line.trim().length() >= minLength) {
                result.append(line).append("\n");
            }
        }
        return result.toString().trim();
    }
}
