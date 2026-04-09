package com.naic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.domain.entity.dto.SysUserDTO;
import com.naic.domain.entity.po.SysUserPO;
import com.naic.domain.entity.vo.SysUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 数据层
 * 
 * @author wangyunong
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserPO>
{
    /**
     * 根据条件分页查询已配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUserVO> selectAllocatedList(SysUserDTO user);

    /**
     * 根据条件分页查询未分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUserVO> selectUnallocatedList(SysUserDTO user);

    /**
     * 批量删除用户信息
     * 
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    public int deleteUserByIds(Long[] userIds);
}
