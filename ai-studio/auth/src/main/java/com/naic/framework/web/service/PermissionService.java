package com.naic.framework.web.service;

import com.naic.common.interceptor.LoginIdHolder;
import com.naic.constant.Constants;
import com.naic.domain.entity.vo.SysRoleVO;
import com.naic.domain.entity.vo.SysUserVO;
import com.naic.framework.security.context.PermissionContextHolder;
import com.naic.service.ISysUserService;
import com.naic.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * 自定义权限实现，ss取自SpringSecurity首字母
 * 
 * @author wangyunong
 */
@Service("ss")
public class PermissionService
{
    private final ISysUserService userService;

    private final SysPermissionService permissionService;

    public PermissionService(ISysUserService userService, SysPermissionService permissionService) {
        this.userService = userService;
        this.permissionService = permissionService;
    }

    /**
     * 验证用户是否具备某权限
     * 
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public boolean hasPermi(String permission)
    {
        if (StringUtils.isEmpty(permission))
        {
            return false;
        }
        if(LoginIdHolder.getLoginId().equals(0L)){
            return false;
        }
        SysUserVO loginUser = userService.selectUserById(LoginIdHolder.getLoginId());
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(permissionService.getMenuPermission(loginUser)))
        {
            return false;
        }
        PermissionContextHolder.setContext(permission);
        return hasPermissions(permissionService.getMenuPermission(loginUser), permission);
    }

    /**
     * 验证用户是否不具备某权限，与 hasPermi逻辑相反
     *
     * @param permission 权限字符串
     * @return 用户是否不具备某权限
     */
    public boolean lacksPermi(String permission)
    {
        return hasPermi(permission) != true;
    }

    /**
     * 验证用户是否具有以下任意一个权限
     *
     * @param permissions 以 PERMISSION_DELIMETER 为分隔符的权限列表
     * @return 用户是否具有以下任意一个权限
     */
    public boolean hasAnyPermi(String permissions)
    {
        if (StringUtils.isEmpty(permissions))
        {
            return false;
        }
        SysUserVO loginUser = userService.selectUserById(LoginIdHolder.getLoginId());
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(permissionService.getMenuPermission(loginUser)))
        {
            return false;
        }
        PermissionContextHolder.setContext(permissions);
        Set<String> authorities = permissionService.getMenuPermission(loginUser);
        for (String permission : permissions.split(Constants.PERMISSION_DELIMETER))
        {
            if (permission != null && hasPermissions(authorities, permission))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否拥有某个角色
     * 
     * @param role 角色字符串
     * @return 用户是否具备某角色
     */
    public boolean hasRole(String role)
    {
        if (StringUtils.isEmpty(role))
        {
            return false;
        }
        SysUserVO loginUser = userService.selectUserById(LoginIdHolder.getLoginId());
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getRoles()))
        {
            return false;
        }
        for (SysRoleVO sysRole : loginUser.getRoles())
        {
            String roleKey = sysRole.getRoleKey();
            if (Constants.SUPER_ADMIN.equals(roleKey) || roleKey.equals(StringUtils.trim(role)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证用户是否不具备某角色，与 isRole逻辑相反。
     *
     * @param role 角色名称
     * @return 用户是否不具备某角色
     */
    public boolean lacksRole(String role)
    {
        return hasRole(role) != true;
    }

    /**
     * 验证用户是否具有以下任意一个角色
     *
     * @param roles 以 ROLE_NAMES_DELIMETER 为分隔符的角色列表
     * @return 用户是否具有以下任意一个角色
     */
    public boolean hasAnyRoles(String roles)
    {
        if (StringUtils.isEmpty(roles))
        {
            return false;
        }
        SysUserVO loginUser = userService.selectUserById(LoginIdHolder.getLoginId());
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getRoles()))
        {
            return false;
        }
        for (String role : roles.split(Constants.ROLE_DELIMETER))
        {
            if (hasRole(role))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否包含权限
     * 
     * @param permissions 权限列表
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    private boolean hasPermissions(Set<String> permissions, String permission)
    {
        return permissions.contains(Constants.ALL_PERMISSION) || permissions.contains(StringUtils.trim(permission));
    }
}
