package com.naic.controller;

import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import com.naic.common.interceptor.LoginIdHolder;
import com.naic.constant.UserConstants;
import com.naic.domain.entity.dto.SysMenuDTO;
import com.naic.domain.entity.vo.SysMenuVO;
import com.naic.domain.entity.vo.MenuListVO;
import com.naic.log.annotation.Log;
import com.naic.service.ISysMenuService;
import com.naic.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单信息
 * 
 * @author wangyunong
 */
@Api(tags = "菜单信息")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/system/menu")
public class SysMenuController
{

    private final ISysMenuService menuService;

    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    @ApiOperation("获取菜单列表")
    @Log()
    public Result<?> list(SysMenuDTO menu)
    {
        List<SysMenuVO> menus = menuService.selectMenuList(menu, LoginIdHolder.getLoginId());
        return Result.success(menus);
    }

    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    @ApiOperation("根据菜单编号获取详细信息")
    @Log()
    public Result<?> getInfo(@PathVariable Long menuId)
    {
        return Result.success(menuService.selectMenuById(menuId));
    }

    @GetMapping("/treeselect")
    @ApiOperation("获取菜单下拉树列表")
    @Log()
    public Result treeselect(SysMenuDTO menu)
    {
        List<SysMenuVO> menus = menuService.selectMenuList(menu, LoginIdHolder.getLoginId());
        return Result.success(menuService.buildMenuTreeSelect(menus));
    }

    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    @ApiOperation("加载对应角色菜单列表树")
    @Log()
    public Result<MenuListVO> roleMenuTreeselect(@PathVariable("roleId") Long roleId)
    {
        List<SysMenuVO> menus = menuService.selectMenuList(LoginIdHolder.getLoginId());
        MenuListVO vo= new MenuListVO();
        vo.setCheckedKeys(menuService.selectMenuListByRoleId(roleId));
        vo.setMenus(menuService.buildMenuTreeSelect(menus));
        return Result.success(vo);
    }

    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @PostMapping
    @ApiOperation("新增菜单")
    @Log()
    public Result<?> add(@Validated @RequestBody SysMenuDTO menu)
    {
        if (!menuService.checkMenuNameUnique(menu))
        {
            throw new ServiceException("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath()))
        {
            throw new ServiceException("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        menuService.insertMenu(menu);
        return Result.success("新增菜单成功");
    }

    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @PutMapping
    @ApiOperation("修改菜单")
    @Log()
    public Result<?> edit(@Validated @RequestBody SysMenuDTO menu)
    {
        if (!menuService.checkMenuNameUnique(menu))
        {
            throw new ServiceException("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath()))
        {
            throw new ServiceException("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        else if (menu.getMenuId().equals(menu.getParentId()))
        {
            throw new ServiceException("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        menuService.updateMenu(menu);
        return Result.success("修改菜单成功");
    }

    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @DeleteMapping("/{menuId}")
    @ApiOperation("删除菜单")
    @Log()
    public Result<?> remove(@PathVariable("menuId") Long menuId)
    {
        if (menuService.hasChildByMenuId(menuId))
        {
            throw new ServiceException("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId))
        {
            throw new ServiceException("菜单已分配,不允许删除");
        }
        menuService.deleteMenuById(menuId);
        return Result.success("删除菜单成功");
    }

    @GetMapping("routers")
    @ApiOperation("获取路由信息")
    @Log()
    public Result<?> getRouters()
    {
        List<SysMenuVO> menus = menuService.selectMenuTreeByUserId(LoginIdHolder.getLoginId());
        return Result.success(menuService.buildMenus(menus));
    }
}