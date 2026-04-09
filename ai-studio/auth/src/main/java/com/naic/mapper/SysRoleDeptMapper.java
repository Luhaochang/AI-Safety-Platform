package com.naic.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.domain.entity.po.SysRoleDeptPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色与部门关联表 数据层
 * 
 * @author wangyunong
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDeptPO>
{
    /**
     * 通过角色ID删除角色和部门关联
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleDeptByRoleId(Long roleId);

    /**
     * 批量删除角色部门关联信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteRoleDept(Long[] ids);

    /**
     * 批量新增角色部门信息
     * 
     * @param roleDeptList 角色部门列表
     * @return 结果
     */
    public int batchRoleDept(List<SysRoleDeptPO> roleDeptList);
}
