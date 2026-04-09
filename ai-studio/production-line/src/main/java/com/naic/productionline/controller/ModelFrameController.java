package com.naic.productionline.controller;

import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.vo.ModelFrameVO;
import com.naic.productionline.service.ModelFrameService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模型框架
 *
 * @author xingdong
 */
@Api(tags = "模型框架管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/model-frame")
public class ModelFrameController {

    private final ModelFrameService ModelFrameService;

    @GetMapping("all")
    @ApiOperation("查询所有")
    @Log()
    public Result<List<ModelFrameVO>> getAll(){
        return Result.success(ModelFrameService.getAll());
    }
}
