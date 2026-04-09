package com.naic.productionline.controller;

import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.vo.ModelProviderVO;
import com.naic.productionline.service.ModelProviderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模型提供方
 *
 * @author xingdong
 */
@Api(tags = "模型提供方管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/model-provider")
public class ModelProviderController {

    private final ModelProviderService ModelProviderService;

    @GetMapping("all")
    @ApiOperation("查询所有")
    @Log()
    public Result<List<ModelProviderVO>> getAll(){
        return Result.success(ModelProviderService.getAll());
    }
}
