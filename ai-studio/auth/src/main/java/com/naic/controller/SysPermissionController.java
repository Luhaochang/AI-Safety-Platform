package com.naic.controller;

import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import com.naic.constant.UserConstants;
import com.naic.domain.entity.dto.SysDeptDTO;
import com.naic.domain.entity.vo.SysDeptVO;
import com.naic.framework.web.service.PermissionService;
import com.naic.log.annotation.Log;
import com.naic.service.ISysDeptService;
import com.naic.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * 自定义权限实现
 * 
 * @author wangyunong
 */
@Api(tags = "自定义权限实现")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/permission")
public class SysPermissionController
{

    private final PermissionService permissionService;

    @GetMapping("/has-permission")
    @ApiOperation("查询是否拥有权限")
    @Log()
    public Result<Boolean> hasPermission(@RequestParam("permission") String permission)
    {
        return Result.success(permissionService.hasPermi(permission));
    }
}
