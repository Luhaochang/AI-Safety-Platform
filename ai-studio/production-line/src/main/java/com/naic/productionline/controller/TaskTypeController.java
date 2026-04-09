package com.naic.productionline.controller;

import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.vo.TaskTypeVO;
import com.naic.productionline.service.TaskTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 任务类型
 *
 * @author xingdong
 */
@Api(tags = "任务类型管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/task-type")
public class TaskTypeController {

    private final TaskTypeService taskTypeService;

    @GetMapping("all")
    @ApiOperation("查询所有")
    @Log()
    public Result<List<TaskTypeVO>> getAll(){
        return Result.success(taskTypeService.getAll());
    }
}
