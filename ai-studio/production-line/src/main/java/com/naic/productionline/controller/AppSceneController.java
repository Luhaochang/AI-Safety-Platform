package com.naic.productionline.controller;

import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.vo.AppSceneVO;
import com.naic.productionline.service.AppSceneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 应用场景
 *
 * @author xingdong
 */
@Api(tags = "应用场景管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/app-scene")
public class AppSceneController {

    private final AppSceneService appSceneService;

    @GetMapping("all")
    @ApiOperation("查询所有")
    @Log()
    public Result<List<AppSceneVO>> getAll(){
        return Result.success(appSceneService.getAll());
    }
}
