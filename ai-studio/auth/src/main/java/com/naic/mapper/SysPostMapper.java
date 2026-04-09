package com.naic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.domain.entity.po.SysPostPO;

import java.util.List;

/**
 * 岗位信息 数据层
 * 
 * @author wangyunong
 */
public interface SysPostMapper extends BaseMapper<SysPostPO>
{

    /**
     * 根据用户ID获取岗位选择框列表
     * 
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    public List<Long> selectPostListByUserId(Long userId);

    /**
     * 查询用户所属岗位组
     * 
     * @param userName 用户名
     * @return 结果
     */
    public List<SysPostPO> selectPostsByUserName(String userName);

    /**
     * 批量删除岗位信息
     * 
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    public int deletePostByIds(Long[] postIds);
}
