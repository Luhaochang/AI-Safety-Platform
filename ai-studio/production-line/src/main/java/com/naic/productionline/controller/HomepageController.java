package com.naic.productionline.controller;


import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.vo.homePage.*;
import com.naic.productionline.service.HomePageService;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页信息
 *
 * @author wangyunong
 */
@Api(tags = "首页信息管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/home-page-statistics")
public class HomepageController {

    private final HomePageService homePageService;

    @GetMapping("data-set")
    @ApiOperation("数据集分类统计")
    @Log()
    public Result<List<DataSetStatistics>> getDataSetStatistics()
    {
        return Result.success(homePageService.getDataSetStatistics());
    }

    @GetMapping("data-set-task")
    @ApiOperation("数据集标注完成率")
    @Log()
    public Result<DataPage<DataSetTaskStatistics>> getDataSetTaskStatistics(@RequestParam Integer pageNo, @RequestParam Integer pageSize)
    {
        return Result.success(homePageService.getDataSetTaskStatistics(pageNo,pageSize));
    }

    @GetMapping("finished-pipeline-task")
    @ApiOperation("已完成模型训练任务的时间情况统计")
    @Log()
    public Result<List<FinishedPipelineTaskStatistics>> getFinishedPipelineTaskStatistics()
    {
        return Result.success(homePageService.getFinishedPipelineTaskStatistics());
    }

    @GetMapping("model")
    @ApiOperation("模型情况统计")
    @Log()
    public Result<List<ModelStatistics>> getModelStatistics()
    {
        return Result.success(homePageService.getModelStatistics());
    }

    @GetMapping("pipeline-task")
    @ApiOperation("模型训练情况统计")
    @Log()
    public Result<List<PipelineTaskStatistics>> getPipelineTaskStatistics()
    {
        return Result.success(homePageService.getPipelineTaskStatistics());
    }

    @GetMapping("summary")
    @ApiOperation("总体情况统计")
    @Log()
    public Result<SummaryStatistics> getSummaryStatistics()
    {
        return Result.success(homePageService.getSummaryStatistics());
    }

    @GetMapping("node")
    @ApiOperation("节点情况统计")
    @Log()
    public Result<DataPage<NodeStatistics>> getSummaryStatistics(@RequestParam Integer pageNo, @RequestParam Integer pageSize) throws ApiException {
        return Result.success(homePageService.getNodeStatistics(pageNo,pageSize));
    }

}
