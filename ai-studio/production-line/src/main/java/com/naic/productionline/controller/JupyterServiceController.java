package com.naic.productionline.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.dto.JupyterServiceDTO;
import com.naic.productionline.domain.dto.ModelServiceDTO;
import com.naic.productionline.domain.po.JupyterImage;
import com.naic.productionline.domain.vo.JupyterServiceVO;
import com.naic.productionline.domain.vo.ModelServiceVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import com.naic.productionline.service.JupyterImageService;
import com.naic.productionline.service.ModelServiceService;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@Api(tags = "jupyter服务管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/jupyter-service")
public class JupyterServiceController {

    private final JupyterImageService jupyterImageService;

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:list')")
    @GetMapping("condition")
    @ApiOperation("条件查询jupyter服务")
    @Log()
    public Result<DataPage<JupyterServiceVO>> getCondition(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo, @RequestParam Integer pageSize){
        return Result.success(jupyterImageService.getJupyterService(condition,pageNo,pageSize));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:list')")
    @GetMapping("jupyter-image")
    @ApiOperation("查询jupyter镜像")
    @Log()
    public Result<List<JupyterImage>> getJupyterImage(){
        return Result.success(jupyterImageService.getJupyterImage());
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:list')")
    @GetMapping("jupyter-by-model")
    @ApiOperation("根据模型id查询jupyter服务")
    @Log()
    public Result<List<JupyterServiceVO>> getJupyterService(@RequestParam Long modelId){
        return Result.success(jupyterImageService.getJupyterServiceByModelId(modelId));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:add')")
    @PostMapping
    @ApiOperation("部署服务")
    @Log()
    public Result<?> insertService(@RequestBody JupyterServiceDTO dto) throws ApiException {
        jupyterImageService.insertService(dto);
        return Result.success("开始部署");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:delete')")
    @DeleteMapping
    @ApiOperation("删除服务")
    @Log()
    public Result<?> deleteService(@RequestParam Long id) throws ApiException {
        jupyterImageService.deleteService(id);
        return Result.success("删除成功");
    }

}
