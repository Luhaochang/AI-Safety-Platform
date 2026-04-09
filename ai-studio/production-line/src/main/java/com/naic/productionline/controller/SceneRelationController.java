package com.naic.productionline.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.cnv.ModelCnv;
import com.naic.productionline.domain.dto.ModelDTO;
import com.naic.productionline.domain.po.ModelPO;
import com.naic.productionline.domain.po.SceneRelation;
import com.naic.productionline.domain.vo.ModelVO;
import com.naic.productionline.service.ModelService;
import com.naic.productionline.service.ModelTagRelationService;
import com.naic.productionline.service.SceneRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@Api(tags = "应用场景管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/model")
public class SceneRelationController {

    private final SceneRelationService sceneRelationService;

    @GetMapping("by-application")
    @ApiOperation("按照应用场景查询")
    @Log()
    public Result<List<SceneRelation>> getByapplicationScene(@RequestParam Integer applicationScene){
        return Result.success(sceneRelationService.getByApplicationScene(applicationScene));
    }

    @GetMapping("by-scene")
    @ApiOperation("按照任务场景查询")
    @Log()
    public Result<List<SceneRelation>> getByScene(@RequestParam Integer sceneId){
        return Result.success(sceneRelationService.getByScene(sceneId));
    }

}
