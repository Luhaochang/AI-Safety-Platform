package com.naic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.annotation.DataScope;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.api.utils.SpringUtils;
import com.naic.common.interceptor.LoginIdHolder;
import com.naic.constant.UserConstants;
import com.naic.domain.entity.cnv.SysRoleCnv;
import com.naic.domain.entity.dto.SysRoleDTO;
import com.naic.domain.entity.po.SysRoleDeptPO;
import com.naic.domain.entity.po.SysRoleMenuPO;
import com.naic.domain.entity.po.SysRolePO;
import com.naic.domain.entity.po.SysUserRolePO;
import com.naic.domain.entity.vo.*;
import com.naic.mapper.SysRoleDeptMapper;
import com.naic.mapper.SysRoleMapper;
import com.naic.mapper.SysRoleMenuMapper;
import com.naic.mapper.SysUserRoleMapper;
import com.naic.service.ISysRoleService;
import com.naic.utils.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色 业务层处理
 * 
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRolePO> implements ISysRoleService
{

    private final SysRoleMapper roleMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysRoleDeptMapper roleDeptMapper;

    private final SysRoleCnv sysRoleCnv;

    /**
     * 根据条件分页查询角色数据
     * 
     * @param condition 条件查询
     * @return 角色数据集合信息
     */
    @Override
    public List<SysRoleVO> selectRoleList(Map<String, Object> condition)
    {
        List<SysRolePO> list=roleMapper.selectList(Condition.getQueryWrapper(condition,SysRolePO.class).orderByAsc("role_sort"));
        List<SysRoleVO> voList=new ArrayList<>();
        for (SysRolePO po:list){
            voList.add(sysRoleCnv.poToVo(po));
        }
        return voList;
    }

    /**
     * 根据用户ID查询角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRoleVO> selectRolesByUserId(Long userId)
    {
        List<SysRolePO> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRoleVO> roles = this.selectRoleAll();
        for (SysRoleVO role : roles)
        {
            for (SysRolePO userRole : userRoles)
            {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue())
                {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 根据用户ID查询权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId)
    {
        List<SysRolePO> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRolePO perm : perms)
        {
            if (StringUtils.isNotNull(perm))
            {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     * 
     * @return 角色列表
     */
    @Override
    public List<SysRoleVO> selectRoleAll()
    {
        Map<String,Object> condition=new HashMap<>();
        condition.put("delFlagEq",0);
        return this.selectRoleList(condition);
    }

    /**
     * 根据用户ID获取角色选择框列表
     * 
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * 通过角色ID查询角色
     * 
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysRoleVO selectRoleById(Long roleId)
    {
        return sysRoleCnv.poToVo(roleMapper.selectById(roleId));
    }

    /**
     * 校验角色名称是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleNameUnique(SysRoleDTO role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        List<SysRolePO> list=roleMapper.selectList(new QueryWrapper<SysRolePO>().eq("del_flag",0).eq("role_name",role.getRoleName()));
        if (list.isEmpty()){
            return UserConstants.UNIQUE;
        }
        SysRolePO info = list.get(0);
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRoleDTO role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        List<SysRolePO> list=roleMapper.selectList(new QueryWrapper<SysRolePO>().eq("del_flag",0).eq("role_key",role.getRoleKey()));
        if(list.isEmpty()){
            return UserConstants.UNIQUE;
        }
        SysRolePO info = list.get(0);
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     * 
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRoleDTO role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && SpringUtils.isAdmin(role.getRoleId()))
        {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     * 
     * @param roleIds 角色id
     */
    @Override
    public void checkRoleDataScope(Long... roleIds)
    {
        if (!LoginIdHolder.isAdmin())
        {
            for (Long roleId : roleIds)
            {
                Map<String,Object> map= new HashMap<>();
                map.put("role_id",roleId);
                List<SysRoleVO> roles = this.selectRoleList(map);
                if (StringUtils.isEmpty(roles))
                {
                    throw new ServiceException("没有权限访问角色数据！");
                }
            }
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId)
    {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * 新增保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int insertRole(SysRoleDTO role)
    {
        SysRolePO po= sysRoleCnv.dtoToPo(role);
        // 新增角色信息
        roleMapper.insert(po);
        role.setRoleId(po.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int updateRole(SysRoleDTO role)
    {
        // 修改角色信息
        SysRolePO po = sysRoleCnv.dtoToPo(role);
        roleMapper.updateById(po);
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 修改角色状态
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int updateRoleStatus(SysRoleDTO role)
    {
        SysRolePO po = sysRoleCnv.dtoToPo(role);
        roleMapper.updateById(po);
        return roleMapper.updateById(po);
    }

    /**
     * 修改数据权限信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int authDataScope(SysRoleDTO role)
    {
        SysRolePO po = sysRoleCnv.dtoToPo(role);
        roleMapper.updateById(po);
        // 修改角色信息
        roleMapper.updateById(po);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(role);
    }

    /**
     * 新增角色菜单信息
     * 
     * @param role 角色对象
     */
    public int insertRoleMenu(SysRoleDTO role)
    {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenuPO> list = new ArrayList<SysRoleMenuPO>();
        for (Long menuId : role.getMenuIds())
        {
            SysRoleMenuPO rm = new SysRoleMenuPO();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0)
        {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    public int insertRoleDept(SysRoleDTO role)
    {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDeptPO> list = new ArrayList<SysRoleDeptPO>();
        for (Long deptId : role.getDeptIds())
        {
            SysRoleDeptPO rd = new SysRoleDeptPO();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int deleteRoleById(Long roleId)
    {
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     * 批量删除角色信息
     * 
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int deleteRoleByIds(Long[] roleIds)
    {
        for (Long roleId : roleIds)
        {
            checkRoleAllowed(new SysRoleDTO().setRoleId(roleId));
            checkRoleDataScope(roleId);
            SysRoleVO role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenu(roleIds);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDept(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     * 取消授权用户角色
     * 
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public int deleteAuthUser(SysUserRolePO userRole)
    {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     * 批量取消授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * 批量选择授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        // 新增用户与角色管理
        List<SysUserRolePO> list = new ArrayList<SysUserRolePO>();
        for (Long userId : userIds)
        {
            SysUserRolePO ur = new SysUserRolePO();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }
}
