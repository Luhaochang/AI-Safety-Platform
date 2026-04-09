package com.naic.controller;


import com.naic.api.api.*;
import com.naic.api.entity.vo.RegisterUserVO;
import com.naic.api.utils.SpringUtils;
import com.naic.common.interceptor.LoginIdHolder;
import com.naic.domain.entity.dto.SysUserDTO;
import com.naic.domain.entity.po.SysUserPO;
import com.naic.domain.entity.po.SysUserRolePO;
import com.naic.domain.entity.vo.*;
import com.naic.framework.web.service.SysPermissionService;
import com.naic.log.annotation.Log;
import com.naic.service.*;
import com.naic.service.auth.AuthService;
import com.naic.service.auth.LoginUserDaoService;
import com.naic.utils.StringUtils;
import com.nimbusds.jose.JOSEException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户信息
 * 
 * @author wangyunong
 */
@Api(tags = "用户信息")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysUserController
{
    private final ISysUserService userService;

    private final ISysRoleService roleService;

    private final ISysDeptService deptService;

    private final ISysPostService postService;

    private final AuthService authService;

    private final LoginUserDaoService loginUserDaoService;

    private final SysPermissionService permissionService;
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    @ApiOperation("获取用户列表")
    @Log()
    public Result<DataPage<SysUserVO>> list(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo, @RequestParam Integer pageSize)
    {
        List<SysUserVO> list = userService.selectUserList(condition);
        DataPage<SysUserVO> page=DataPage.rest(list,pageNo,pageSize);
        return Result.success(page);
    }

    @GetMapping
    @ApiOperation("个人信息")
    @Log()
    public Result<?> profile()
    {
        Long userId= LoginIdHolder.getLoginId();
        SysUserVO user= userService.selectUserById(userId);
        SysUserProfileVO vo=new SysUserProfileVO();
        vo.setUser(user);

        vo.setRoleGroup(userService.selectUserRoleGroup(user.getUserName()));
        vo.setPostGroup(userService.selectUserPostGroup(user.getUserName()));
        return Result.success(vo);
    }

    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = {"/by-id" })
    @ApiOperation("根据用户编号获取详细信息")
    @Log()
    public Result<SysUserInfoVO> getInfo(@RequestParam(value = "userId",required = false) Long userId)
    {
        userService.checkUserDataScope(userId);
        List<SysRoleVO> roles = roleService.selectRoleAll();
        SysUserInfoVO vo=new SysUserInfoVO();
        vo.setRoles(SpringUtils.isAdmin(userId)? roles : roles.stream().filter(r -> !SpringUtils.isAdmin(r.getRoleId())).collect(Collectors.toList()));
        vo.setPosts(postService.selectPostAll());
        if (StringUtils.isNotNull(userId)) {
            SysUserVO sysUser = userService.selectUserById(userId);
            vo.setData(sysUser);
            vo.setPostIds(postService.selectPostListByUserId(userId));
            vo.setRoleIds(sysUser.getRoles().stream().map(SysRoleVO::getRoleId).collect(Collectors.toList()));
        }
        return Result.success(vo);
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "账号名", required = true, paramType = "body"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "body"),
            @ApiImplicitParam(name = "tel", value = "电话号码", required = true, paramType = "body"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, paramType = "body"),
            @ApiImplicitParam(name = "nickName", value = "昵称", required = true, paramType = "body")
    })
    public Result<?> register(@RequestBody Map<String, String> map){
        userService.register(map);
        return Result.success();
    }

    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @PostMapping
    @ApiOperation("新增用户")
    @Log()
    public Result<?> add(@Validated @RequestBody SysUserDTO user)
    {
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user))
        {
            throw new ServiceException("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhoneNumber()) && !userService.checkPhoneUnique(user))
        {
            throw new ServiceException("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            throw new ServiceException("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return Result.success(userService.insertUser(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @PutMapping("/info")
    @ApiOperation("修改用户基本信息")
    @Log()
    public Result<?> editInfo(@RequestBody SysUserDTO user)
    {
        Long userId=LoginIdHolder.getLoginId();
        SysUserPO loginUser= userService.getById(userId);
        loginUser.setNickName(user.getNickName());
        loginUser.setEmail(user.getEmail());
        loginUser.setPhoneNumber(user.getPhoneNumber());
        loginUser.setSex(user.getSex());
        loginUser.setAvatar(user.getAvatar());
        if (StringUtils.isNotEmpty(user.getPhoneNumber()) && !userService.checkPhoneUnique(user))
        {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        return Result.success(userService.updateById(loginUser));
    }

    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @PutMapping
    @ApiOperation("修改用户")
    @Log()
    public Result<?> edit(@Validated @RequestBody SysUserDTO user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user))
        {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhoneNumber()) && !userService.checkPhoneUnique(user))
        {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        return Result.success(userService.updateUser(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @DeleteMapping("/{userIds}")
    @ApiOperation("删除用户")
    @Log()
    public Result<?> remove(@PathVariable Long[] userIds)
    {
        if (ArrayUtils.contains(userIds, LoginIdHolder.getLoginId()))
        {
            throw new ServiceException("当前用户不能删除");
        }
        return Result.success(userService.deleteUserByIds(userIds));
    }

    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @PutMapping("/resetPwd")
    @ApiOperation("重置密码")
    @Log()
    public Result<?> resetPwd(@RequestBody Map<String, String> map)
    {
        userService.resetPwd(map);

        return Result.success("修改成功");
    }

    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @PutMapping("/changeStatus")
    @ApiOperation("状态修改")
    @Log()
    public Result<?> changeStatus(@RequestBody SysUserDTO user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        return Result.success(userService.updateUserStatus(user));
    }

    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    @ApiOperation("根据用户编号获取授权角色")
    @Log()
    public Result<?> authRole(@PathVariable("userId") Long userId)
    {
        SysUserVO user = userService.selectUserById(userId);
        List<SysRoleVO> roles = roleService.selectRolesByUserId(userId);
        SysUserInfoVO vo=new SysUserInfoVO();
        vo.setData(user);
        vo.setPostIds(postService.selectPostListByUserId(userId));
        vo.setRoleIds(user.getRoles().stream().map(SysRoleVO::getRoleId).collect(Collectors.toList()));
        vo.setRoles(SpringUtils.isAdmin(userId) ? roles : roles.stream().filter(r -> !SpringUtils.isAdmin(r.getRoleId())).collect(Collectors.toList()));
        return Result.success(vo);
    }

    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @PutMapping("/authRole")
    @ApiOperation("用户授权角色")
    @Log()
    public Result<?> insertAuthRole(Long userId, Long[] roleIds)
    {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        userService.insertUserAuth(userId, roleIds);
        return Result.success("授权成功");
    }

    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/deptTree")
    @ApiOperation("获取部门树列表")
    @Log()
    public Result<?> deptTree(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition)
    {
        return Result.success(deptService.selectDeptTreeList(condition));
    }

    @PostMapping("/password/reset")
    @ApiOperation(value = "忘记密码", notes = "body参数username, newPassword, code")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tel", value = "手机号", required = true, paramType = "body"),
            @ApiImplicitParam(name = "newPassword", value = "密码", required = true, paramType = "body"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, paramType = "body")
    })
    @Log()
    public Result<?> setPassword(@RequestBody Map<String, String> map){
        userService.setPassword(map);
        return Result.success();
    }

    @PostMapping("/refresh")
    @ApiOperation("刷新token")
    @Log()
    public Result<String> refreshToken(@RequestBody Map<String, String> map) throws ParseException, JOSEException {
        String ticket = map.get("ticket");
        if (ticket == null){
            return Result.error("ticket must be given");
        }
        return Result.success(authService.refreshToken(ticket));
    }

    @PostMapping("/logout")
    @ApiOperation("退出登录")
    @Log()
    public Result<?> logout(@RequestParam Long userId){
        loginUserDaoService.removeUserById(userId);
        return Result.success();
    }

    @PostMapping("/load")
    @ApiOperation(value = "ticket获取登录信息", notes = "切换站点时使用")
    @Log()
    public Result<RegisterUserVO> load(@RequestBody Map<String, String> map){
        String ticket = map.get("ticket");
        if (ticket == null){
            return Result.error("ticket must be given");
        }
        RegisterUserVO user = loginUserDaoService.loadUserByTicket(ticket);
        if (user == null) {
            Assert.fail(ResultCode.UN_AUTHORIZED);
        }
        return Result.success(user);
    }

    @GetMapping("getInfo")
    @ApiOperation("获取用户信息")
    @Log()
    public Result getInfo()
    {
        SysUserVO user = userService.selectUserById(LoginIdHolder.getLoginId());
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        UserInfoVO vo=new UserInfoVO();
        vo.setUser(user);
        vo.setRoles(roles);
        vo.setPermissions(permissions);
        return Result.success(vo);
    }
}
