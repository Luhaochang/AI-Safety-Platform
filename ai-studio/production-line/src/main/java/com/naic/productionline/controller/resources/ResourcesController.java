package com.naic.productionline.controller.resources;

import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.service.minio.ResourcesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 资源列表 前端控制器
 * </p>*
 * @author wangyunong
 * @data 2023/10/25
 */
@Api(tags = "资源列表")
@Slf4j
@RestController
@RequestMapping("/resources")
@AllArgsConstructor
public class ResourcesController {

    private final ResourcesService resourcesService;

    @PostMapping("/upload-file")
    @ApiOperation(value = "文件上传")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        String filePath = null;
        try {
            filePath = resourcesService.putFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传失败", e);
            return Result.success("上传失败");
        }
        return Result.success(filePath);
    }

    @PostMapping("/upload-dataset")
    @ApiOperation(value = "数据集文件上传")
    public Result<?> uploadDataset(@RequestParam("file") MultipartFile file,@RequestParam String catalogue) {
        String filePath = null;
        try {
            filePath = resourcesService.uploadDataset(file,catalogue);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传失败", e);
            return Result.success("上传失败");
        }
        return Result.success(filePath);
    }

    @GetMapping("/dataset")
    @ApiOperation(value = "获取指定目录下的数据集文件")
    public Result<?> getDataset(@RequestParam String catalogue) {
        List<String> filePath = new ArrayList<>();
        try {
            filePath = resourcesService.getDatasetFile(catalogue);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传失败", e);
            return Result.success("上传失败");
        }
        return Result.success(filePath);
    }

    @PostMapping("/delete-file")
    @ApiOperation(value = "删除数据集指定文件")
    public Result<?> deleteDatasetFile(@RequestParam("file") MultipartFile file,@RequestParam String catalogue,@RequestParam Long datasetId) {
        try {
            resourcesService.deleteDatasetFile(catalogue,file,datasetId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("修改失败", e);
            return Result.success("修改失败");
        }
        return Result.success("修改完成");
    }

    @PostMapping("/zip-dataset")
    @ApiOperation(value = "打包数据集文件")
    public Result<?> zipDataset(@RequestParam Long datasetId) throws Exception {
        resourcesService.zipDataset(datasetId);
        return Result.success("文件打包中，请耐心等待");
    }

    @PostMapping("/get-zip")
    @ApiOperation(value = "数据集文件压缩包下载")
    @Log()
    public Result<String> getFile(@RequestParam Long datasetId){
        String path=resourcesService.getPath(datasetId);
        return Result.success(path);
    }
}
