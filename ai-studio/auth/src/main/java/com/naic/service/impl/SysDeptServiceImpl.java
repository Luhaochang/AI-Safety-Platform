package com.naic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.annotation.DataScope;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.api.utils.SpringUtils;
import com.naic.common.interceptor.LoginIdHolder;
import com.naic.constant.UserConstants;
import com.naic.domain.TreeSelect;
import com.naic.domain.entity.cnv.SysDeptCnv;
import com.naic.domain.entity.dto.SysDeptDTO;
import com.naic.domain.entity.po.SysDeptPO;
import com.naic.domain.entity.po.SysRolePO;
import com.naic.domain.entity.po.SysUserPO;
import com.naic.domain.entity.vo.SysDeptVO;
import com.naic.domain.text.Convert;
import com.naic.mapper.SysDeptMapper;
import com.naic.mapper.SysRoleMapper;
import com.naic.mapper.SysUserMapper;
import com.naic.service.ISysDeptService;
import com.naic.utils.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理 服务实现
 * 
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptPO> implements ISysDeptService
{
    private final SysDeptMapper deptMapper;

    private final SysRoleMapper roleMapper;

    private final SysDeptCnv sysDeptCnv;

    private final SysUserMapper sysUserMapper;
    /**
     * 条件查询部门管理数据
     * 
     * @param condition 条件查询
     * @return 部门信息集合
     */
    @Override
    public List<SysDeptVO> selectDeptList(Map<String, Object> condition)
    {
        List<SysDeptPO> list=new ArrayList<>();
        if (condition!=null) {
            condition.put("delFlagEq", 0);
            list=deptMapper.selectList(Condition.getQueryWrapper(condition,SysDeptPO.class));
        }
        else {
            Map<String, Object> con=new HashMap<>();
            con.put("delFlagEq", 0);
            list=deptMapper.selectList(Condition.getQueryWrapper(con,SysDeptPO.class));
        }
        List<SysDeptVO> voList=new ArrayList<SysDeptVO>();
        for(SysDeptPO po:list){
            voList.add(sysDeptCnv.poToVo(po));
        }
        return voList;
    }

    /**
     * 查询部门树结构信息
     * 
     * @param condition 条件查询
     * @return 部门树信息集合
     */
    @Override
    public List<TreeSelect> selectDeptTreeList(Map<String, Object> condition)
    {
        List<SysDeptVO> deptList = this.selectDeptList(condition);
        return buildDeptTreeSelect(deptList);
    }

    /**
     * 根据角色ID查询部门树信息
     * 
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId)
    {
        SysRolePO role = roleMapper.selectById(roleId);
        return deptMapper.selectDeptListByRoleId(roleId, role.isDeptCheckStrictly());
    }

    /**
     * 根据部门ID查询信息
     * 
     * @param deptId 部门ID
     * @return 部门信息
     */
    @Override
    public SysDeptVO selectDeptById(Long deptId)
    {
        return sysDeptCnv.poToVo(deptMapper.selectById(deptId));
    }

    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param deptId 部门ID
     * @return 子部门数
     */
    @Override
    public int selectNormalChildrenDeptById(Long deptId)
    {
        return deptMapper.selectList(new QueryWrapper<SysDeptPO>().eq("parent_id",deptId).eq("del_flag",0)).size();
    }

    /**
     * 是否存在子节点
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public boolean hasChildByDeptId(Long deptId)
    {
        int result = deptMapper.selectList(new QueryWrapper<SysDeptPO>().eq("parent_id",deptId).eq("del_flag",0)).size();
        return result > 0;
    }

    /**
     * 查询部门是否存在用户
     * 
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkDeptExistUser(Long deptId)
    {
        int result = sysUserMapper.selectList(new QueryWrapper<SysUserPO>().eq("del_flag",0).eq("dept_id",deptId)).size();
        return result > 0;
    }

    /**
     * 校验部门名称是否唯一
     * 
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public boolean checkDeptNameUnique(SysDeptDTO dept)
    {
        Long deptId = StringUtils.isNull(dept.getDeptId()) ? -1L : dept.getDeptId();
        List<SysDeptPO> info = deptMapper.selectList(new QueryWrapper<SysDeptPO>().eq("del_flag",0 ).eq("dept_name",dept.getDeptName()).eq("parent_id",dept.getParentId()));
        if (info.isEmpty()){
            return UserConstants.UNIQUE;
        }
        if (info.get(0).getDeptId().longValue() != deptId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验部门是否有数据权限
     * 
     * @param deptId 部门id
     */
    @Override
    public void checkDeptDataScope(Long deptId)
    {
        if (!LoginIdHolder.isAdmin() && StringUtils.isNotNull(deptId))
        {
            Map<String, Object> condition=new HashMap<>();
            condition.put("deptIdEq",deptId);
            List<SysDeptVO> depts = this.selectDeptList(condition);
            if (StringUtils.isEmpty(depts))
            {
                throw new ServiceException("没有权限访问部门数据！");
            }
        }
    }

    /**
     * 新增保存部门信息
     * 
     * @param dto 部门信息
     * @return 结果
     */
    @Override
    public int insertDept(SysDeptDTO dto)
    {
        SysDeptPO info = deptMapper.selectById(dto.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (!UserConstants.DEPT_NORMAL.equals(info.getStatus()))
        {
            throw new ServiceException("部门停用，不允许新增");
        }
        SysDeptPO po=sysDeptCnv.dtoToPo(dto);
        po.setDelFlag("0");
        po.setAncestors(info.getAncestors() + "," + po.getParentId());
        return deptMapper.insert(po);
    }

    /**
     * 修改保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int updateDept(SysDeptDTO dept)
    {
        SysDeptPO newParentDept = deptMapper.selectById(dept.getParentId());
        SysDeptPO oldDept = deptMapper.selectById(dept.getDeptId());
        SysDeptPO newDept= sysDeptCnv.dtoToPo(dept);
        if (StringUtils.isNotNull(newParentDept) && StringUtils.isNotNull(oldDept))
        {
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
            String oldAncestors = oldDept.getAncestors();
            newDept.setAncestors(newAncestors);
            updateDeptChildren(newDept.getDeptId(), newAncestors, oldAncestors);
        }
        int result = deptMapper.updateById(newDept);
        if (UserConstants.DEPT_NORMAL.equals(newDept.getStatus()) && StringUtils.isNotEmpty(newDept.getAncestors())
                && !StringUtils.equals("0", newDept.getAncestors()))
        {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDeptStatusNormal(newDept);
        }
        return result;
    }

    /**
     * 修改该部门的父级部门状态
     * 
     * @param dept 当前部门
     */
    private void updateParentDeptStatusNormal(SysDeptPO dept)
    {
        String ancestors = dept.getAncestors();
        Long[] deptIds = Convert.toLongArray(ancestors);
        deptMapper.updateDeptStatusNormal(deptIds);
    }

    /**
     * 修改子元素关系
     * 
     * @param deptId 被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors)
    {
        List<SysDeptPO> children = deptMapper.selectChildrenDeptById(deptId);
        for (SysDeptPO child : children)
        {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0)
        {
            deptMapper.updateDeptChildren(children);
        }
    }

    /**
     * 删除部门管理信息
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public int deleteDeptById(Long deptId)
    {
        SysDeptPO po=deptMapper.selectById(deptId);
        po.setDelFlag("1");
        return deptMapper.updateById(po);
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param deptList 部门列表
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildDeptTreeSelect(List<SysDeptVO> deptList)
    {
        List<SysDeptVO> deptTrees = buildDeptTree(deptList);
        return deptTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 构建前端所需要树结构
     *
     * @param deptList 部门列表
     * @return 树结构列表
     */
    public List<SysDeptVO> buildDeptTree(List<SysDeptVO> deptList)
    {
        List<SysDeptVO> returnList = new ArrayList<SysDeptVO>();
        List<Long> tempList = deptList.stream().map(SysDeptVO::getDeptId).collect(Collectors.toList());
        for (SysDeptVO dept : deptList)
        {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(dept.getParentId()))
            {
                recursionFn(deptList, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = deptList;
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysDeptVO> list, SysDeptVO t)
    {
        // 得到子节点列表
        List<SysDeptVO> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysDeptVO tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysDeptVO> getChildList(List<SysDeptVO> list, SysDeptVO t)
    {
        List<SysDeptVO> tlist = new ArrayList<SysDeptVO>();
        Iterator<SysDeptVO> it = list.iterator();
        while (it.hasNext())
        {
            SysDeptVO n = (SysDeptVO) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysDeptVO> list, SysDeptVO t)
    {
        return getChildList(list, t).size() > 0;
    }
}
