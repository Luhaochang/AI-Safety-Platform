package com.naic.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.naic.domain.TreeSelect;
import com.naic.domain.entity.dto.SysDeptDTO;
import com.naic.domain.entity.po.SysDeptPO;
import com.naic.domain.entity.vo.SysDeptVO;

import java.util.List;
import java.util.Map;

/**
 * 部门管理 服务层
 * 
 * @author wangyunong
 */
public interface ISysDeptService extends IService<SysDeptPO>
{
    /**
     * 条件查询部门管理数据
     * 
     * @param condition 条件
     * @return 部门信息集合
     */
    public List<SysDeptVO> selectDeptList(Map<String, Object> condition);

    /**
     * 查询部门树结构信息
     * 
     * @param condition 条件
     * @return 部门树信息集合
     */
    public List<TreeSelect> selectDeptTreeList(Map<String, Object> condition);

    /**
     * 根据角色ID查询部门树信息
     * 
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    public List<Long> selectDeptListByRoleId(Long roleId);

    /**
     * 根据部门ID查询信息
     * 
     * @param deptId 部门ID
     * @return 部门信息
     */
    public SysDeptVO selectDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param deptId 部门ID
     * @return 子部门数
     */
    public int selectNormalChildrenDeptById(Long deptId);

    /**
     * 是否存在部门子节点
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    public boolean hasChildByDeptId(Long deptId);

    /**
     * 查询部门是否存在用户
     * 
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    public boolean checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     * 
     * @param dept 部门信息
     * @return 结果
     */
    public boolean checkDeptNameUnique(SysDeptDTO dept);

    /**
     * 校验部门是否有数据权限
     * 
     * @param deptId 部门id
     */
    public void checkDeptDataScope(Long deptId);

    /**
     * 新增保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    public int insertDept(SysDeptDTO dept);

    /**
     * 修改保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    public int updateDept(SysDeptDTO dept);

    /**
     * 删除部门管理信息
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    public int deleteDeptById(Long deptId);
}
