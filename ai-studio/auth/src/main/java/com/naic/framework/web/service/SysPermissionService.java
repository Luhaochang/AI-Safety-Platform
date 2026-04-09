package com.naic.framework.web.service;

import com.naic.api.utils.SpringUtils;
import com.naic.domain.entity.vo.SysRoleVO;
import com.naic.domain.entity.vo.SysUserVO;
import com.naic.service.ISysMenuService;
import com.naic.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户权限处理
 * 
 * @author wangyunong
 */
@Component
public class SysPermissionService
{
    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysMenuService menuService;

    /**
     * 获取角色数据权限
     * 
     * @param user 用户信息
     * @return 角色权限信息
     */
    public Set<String> getRolePermission(SysUserVO user)
    {
        Set<String> roles = new HashSet<String>();
        // 管理员拥有所有权限
        if (SpringUtils.isAdmin(user.getUserId()))
        {
            roles.add("admin");
        }
        else
        {
            roles.addAll(roleService.selectRolePermissionByUserId(user.getUserId()));
        }
        return roles;
    }

    /**
     * 获取菜单数据权限
     * 
     * @param user 用户信息
     * @return 菜单权限信息
     */
    public Set<String> getMenuPermission(SysUserVO user)
    {
        Set<String> perms = new HashSet<String>();
        // 管理员拥有所有权限
        if (SpringUtils.isAdmin(user.getUserId()))
        {
            perms.add("*:*:*");
        }
        else
        {
            List<SysRoleVO> roles = user.getRoles();
            if (!CollectionUtils.isEmpty(roles))
            {
                // 多角色设置permissions属性，以便数据权限匹配权限
                for (SysRoleVO role : roles)
                {
                    Set<String> rolePerms = menuService.selectMenuPermsByRoleId(role.getRoleId());
                    role.setPermissions(rolePerms);
                    perms.addAll(rolePerms);
                }
            }
            else
            {
                perms.addAll(menuService.selectMenuPermsByUserId(user.getUserId()));
            }
        }
        return perms;
    }
}
