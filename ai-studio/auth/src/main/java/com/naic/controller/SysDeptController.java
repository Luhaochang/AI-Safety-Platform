package com.naic.controller;

import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import com.naic.constant.UserConstants;
import com.naic.domain.entity.dto.SysDeptDTO;
import com.naic.domain.entity.vo.SysDeptVO;
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
 * 部门信息
 * 
 * @author wangyunong
 */
@Api(tags = "部门信息")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/system/dept")
public class SysDeptController
{

    private final ISysDeptService deptService;

    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    @ApiOperation("获取部门列表")
    @Log()
    public Result<List<SysDeptVO>> list(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition)
    {
        List<SysDeptVO> deptList = deptService.selectDeptList(condition);
        return Result.success(deptList);
    }

    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude")
    @ApiOperation("查询部门列表（排除节点）")
    @Log()
    public Result<List<SysDeptVO>> excludeChild(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition,@RequestParam(value = "deptId", required = false) Long deptId)
    {
        List<SysDeptVO> deptList = deptService.selectDeptList(condition);
        deptList.removeIf(d -> d.getDeptId().intValue() == deptId || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
        return Result.success(deptList);
    }

    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    @ApiOperation("根据部门编号获取详细信息")
    @Log()
    public Result<SysDeptVO> getInfo(@PathVariable Long deptId)
    {
        deptService.checkDeptDataScope(deptId);
        return Result.success(deptService.selectDeptById(deptId));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @PostMapping
    @ApiOperation("新增部门")
    @Log()
    public Result<?> add(@Validated @RequestBody SysDeptDTO dept)
    {
        if (!deptService.checkDeptNameUnique(dept))
        {
            throw new ServiceException("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        return Result.success(deptService.insertDept(dept));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @PutMapping
    @ApiOperation("修改部门")
    @Log()
    public Result<?> edit(@Validated @RequestBody SysDeptDTO dept)
    {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept))
        {
            throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        else if (dept.getParentId().equals(deptId))
        {
            throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0)
        {
            throw new ServiceException("该部门包含未停用的子部门！");
        }
        return Result.success(deptService.updateDept(dept));
    }

    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @DeleteMapping("/{deptId}")
    @ApiOperation("删除部门")
    @Log()
    public Result<?> remove(@PathVariable Long deptId)
    {
        if (deptService.hasChildByDeptId(deptId))
        {
            throw new ServiceException("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId))
        {
            throw new ServiceException("部门存在用户,不允许删除");
        }
        deptService.checkDeptDataScope(deptId);
        return Result.success(deptService.deleteDeptById(deptId));
    }
}
