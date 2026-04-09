package com.naic.productionline.controller;

import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.po.ClusterNodePO;
import com.naic.productionline.mapper.ClusterNodeMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "数字孪生")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/digital-twin")
public class DigitalTwinController {

    private final ClusterNodeMapper clusterNodeMapper;

    @GetMapping("cluster/realtime")
    @ApiOperation("获取集群节点实时状态")
    @Log()
    public Result<Map<String, Object>> getClusterRealtime() {
        List<ClusterNodePO> nodes = clusterNodeMapper.selectList(null);

        Random rand = new Random();
        List<Map<String, Object>> nodeList = nodes.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("name", n.getName());
            map.put("ip", n.getIp());
            map.put("status", n.getStatus());

            boolean isOffline = "offline".equals(n.getStatus());

            Map<String, Object> cpu = new HashMap<>();
            cpu.put("cores", n.getCpuCores());
            cpu.put("usage", isOffline ? 0 : 20 + rand.nextInt(60));
            map.put("cpu", cpu);

            Map<String, Object> gpu = new HashMap<>();
            gpu.put("count", n.getGpuCount());
            gpu.put("model", n.getGpuModel());
            gpu.put("usage", isOffline ? 0 : 30 + rand.nextInt(60));
            gpu.put("memUsage", isOffline ? 0 : 25 + rand.nextInt(55));
            gpu.put("temperature", isOffline ? 30 : 55 + rand.nextInt(25));
            map.put("gpu", gpu);

            Map<String, Object> memory = new HashMap<>();
            memory.put("total", n.getMemoryTotal());
            memory.put("used", isOffline ? 0 : (int) (n.getMemoryTotal() * (0.3 + rand.nextDouble() * 0.5)));
            map.put("memory", memory);

            Map<String, Object> disk = new HashMap<>();
            disk.put("total", n.getDiskTotal());
            disk.put("used", (int) (n.getDiskTotal() * (0.3 + rand.nextDouble() * 0.4)));
            map.put("disk", disk);

            map.put("tasks", Collections.emptyList());

            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodeList);
        result.put("timestamp", System.currentTimeMillis());
        return Result.success(result);
    }

    @GetMapping("alerts")
    @ApiOperation("获取告警列表(硬编码)")
    @Log()
    public Result<List<Map<String, Object>>> getAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();

        Map<String, Object> a1 = new HashMap<>();
        a1.put("id", 1);
        a1.put("nodeId", "node-04");
        a1.put("nodeName", "Worker-03");
        a1.put("level", "warning");
        a1.put("type", "GPU温度过高");
        a1.put("message", "GPU #2 温度达到87°C，接近阈值90°C");
        a1.put("status", "active");
        a1.put("time", "2025-03-12 10:25:00");
        alerts.add(a1);

        Map<String, Object> a2 = new HashMap<>();
        a2.put("id", 2);
        a2.put("nodeId", "node-04");
        a2.put("nodeName", "Worker-03");
        a2.put("level", "warning");
        a2.put("type", "磁盘空间不足");
        a2.put("message", "磁盘使用率达90%，剩余400GB");
        a2.put("status", "active");
        a2.put("time", "2025-03-12 09:50:00");
        alerts.add(a2);

        Map<String, Object> a3 = new HashMap<>();
        a3.put("id", 3);
        a3.put("nodeId", "node-06");
        a3.put("nodeName", "Inference-02");
        a3.put("level", "critical");
        a3.put("type", "节点离线");
        a3.put("message", "节点已离线超过30分钟");
        a3.put("status", "active");
        a3.put("time", "2025-03-12 09:30:00");
        alerts.add(a3);

        Map<String, Object> a4 = new HashMap<>();
        a4.put("id", 4);
        a4.put("nodeId", "node-01");
        a4.put("nodeName", "Master-01");
        a4.put("level", "info");
        a4.put("type", "任务完成");
        a4.put("message", "YOLOv8训练任务预计20分钟内完成");
        a4.put("status", "resolved");
        a4.put("time", "2025-03-12 10:20:00");
        alerts.add(a4);

        return Result.success(alerts);
    }

    @GetMapping("scene/config")
    @ApiOperation("获取场景配置")
    @Log()
    public Result<Map<String, Object>> getSceneConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("refreshInterval", 8000);
        config.put("enableAlerts", true);
        config.put("enable3D", true);
        return Result.success(config);
    }
}
