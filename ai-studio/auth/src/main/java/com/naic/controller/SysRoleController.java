package com.naic.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import com.naic.common.interceptor.LoginIdHolder;
import com.naic.domain.entity.dto.SysRoleDTO;
import com.naic.domain.entity.dto.SysUserDTO;
import com.naic.domain.entity.po.SysUserRolePO;
import com.naic.domain.entity.vo.*;
import com.naic.framework.web.service.SysPermissionService;
import com.naic.log.annotation.Log;
import com.naic.service.ISysDeptService;
import com.naic.service.ISysRoleService;
import com.naic.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * 角色信息
 * 
 * @author wangyunong
 */
@Api(tags = "角色信息")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/system/role")
public class SysRoleController
{
    private final ISysRoleService roleService;

    private final SysPermissionService permissionService;

    private final ISysUserService userService;

    private final ISysDeptService deptService;

    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @PostMapping("/list")
    @ApiOperation("获取角色列表")
    @Log()
    public Result<DataPage<SysRoleVO>> list(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo, @RequestParam Integer pageSize)
    {
        List<SysRoleVO> list = roleService.selectRoleList(condition);
        return Result.success(DataPage.rest(list,pageNo,pageSize));
    }

    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/{roleId}")
    @ApiOperation("根据角色编号获取详细信息")
    @Log()
    public Result<SysRoleVO> getInfo(@PathVariable Long roleId)
    {
        roleService.checkRoleDataScope(roleId);
        return Result.success(roleService.selectRoleById(roleId));
    }

    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @PostMapping
    @ApiOperation("新增角色")
    @Log()
    public Result<?> add(@Validated @RequestBody SysRoleDTO role)
    {
        if (!roleService.checkRoleNameUnique(role))
        {
            throw new ServiceException("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            throw new ServiceException("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        roleService.insertRole(role);
        return Result.success();

    }

    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping
    @ApiOperation("修改保存角色")
    @Log()
    public Result<?> edit(@Validated @RequestBody SysRoleDTO role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        if (!roleService.checkRoleNameUnique(role))
        {
            throw new ServiceException("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            throw new ServiceException("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        
        if (roleService.updateRole(role) > 0)
        {
            // 更新缓存用户权限
            Long loginUserId= LoginIdHolder.getLoginId();
            if (!loginUserId.equals(0L)&&!LoginIdHolder.isAdmin())
            {
               SysUserVO user=userService.selectUserById(loginUserId);
               permissionService.getMenuPermission(user);
               userService.selectUserByUserName(user.getUserName());
            }
            return Result.success("修改角色成功");
        }
        throw new ServiceException("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }


    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/data-scope")
    @ApiOperation("修改保存数据权限")
    @Log()
    public Result<?> dataScope(@RequestBody SysRoleDTO role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return Result.success(roleService.authDataScope(role));
    }

    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/change-status")
    @ApiOperation("状态修改")
    @Log()
    public Result<?> changeStatus(@RequestBody SysRoleDTO role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return Result.success(roleService.updateRoleStatus(role));
    }

    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    @DeleteMapping("/{roleIds}")
    @ApiOperation("删除角色")
    @Log()
    public Result<?> remove(@PathVariable Long[] roleIds)
    {
        return Result.success(roleService.deleteRoleByIds(roleIds));
    }

    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping("/optionselect")
    @ApiOperation("获取角色选择框列表")
    @Log()
    public Result<?> optionselect()
    {
        return Result.success(roleService.selectRoleAll());
    }

    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/allocatedList")
    @ApiOperation("查询已分配用户角色列表")
    @Log()
    public Result<DataPage<SysUserVO>> allocatedList(SysUserDTO user, @RequestParam Integer pageNo, @RequestParam Integer pageSize)
    {
        List<SysUserVO> list = userService.selectAllocatedList(user);
        return Result.success(DataPage.rest(list,pageNo,pageSize));
    }

    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/unallocatedList")
    @ApiOperation("查询未分配用户角色列表")
    @Log()
    public Result<DataPage<SysUserVO>> unallocatedList(SysUserDTO user, @RequestParam Integer pageNo, @RequestParam Integer pageSize)
    {
        List<SysUserVO> list = userService.selectUnallocatedList(user);
        return Result.success(DataPage.rest(list,pageNo,pageSize));
    }

    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/authUser/cancel")
    @ApiOperation("取消授权用户")
    @Log()
    public Result<?> cancelAuthUser(@RequestBody SysUserRolePO userRole)
    {
        return Result.success(roleService.deleteAuthUser(userRole));
    }

    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/authUser/cancelAll")
    @ApiOperation("批量取消授权用户")
    @Log()
    public Result<?> cancelAuthUserAll(Long roleId, Long[] userIds)
    {
        return Result.success(roleService.deleteAuthUsers(roleId, userIds));
    }

    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/authUser/selectAll")
    @ApiOperation("批量选择用户授权")
    @Log()
    public Result<?> selectAuthUserAll(Long roleId, Long[] userIds)
    {
        roleService.checkRoleDataScope(roleId);
        return Result.success(roleService.insertAuthUsers(roleId, userIds));
    }

    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/deptTree/{roleId}")
    @ApiOperation("获取对应角色部门树列表")
    @Log()
    public Result<?> deptTree(@PathVariable("roleId") Long roleId)
    {
        SysDeptTreeVO vo=new SysDeptTreeVO();
        vo.setCheckedKeys(deptService.selectDeptListByRoleId(roleId));
        vo.setDeptList(deptService.selectDeptTreeList(null));
        return Result.success(vo);
    }
}
