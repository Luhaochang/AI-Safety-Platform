package com.naic.controller.resources;

import com.naic.api.api.Result;
import com.naic.service.minio.ResourcesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}
