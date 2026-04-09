package com.naic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.constant.UserConstants;
import com.naic.domain.entity.cnv.SysPostCnv;
import com.naic.domain.entity.dto.SysPostDTO;
import com.naic.domain.entity.po.SysPostPO;
import com.naic.domain.entity.vo.SysPostVO;
import com.naic.mapper.SysPostMapper;
import com.naic.mapper.SysUserPostMapper;
import com.naic.service.ISysPostService;
import com.naic.utils.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 岗位信息 服务层处理
 * 
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper,SysPostPO> implements ISysPostService
{
    private final SysPostMapper postMapper;

    private final SysUserPostMapper userPostMapper;

    private final SysPostCnv sysPostCnv;
    /**
     * 查询岗位信息集合
     * 
     * @param condition 条件
     * @return 岗位信息集合
     */
    @Override
    public List<SysPostVO> selectPostList(Map<String, Object> condition)
    {
        List<SysPostPO> list=postMapper.selectList(Condition.getQueryWrapper(condition, SysPostPO.class));
        List<SysPostVO> voList=new ArrayList<>();
        for(SysPostPO po:list){
            voList.add(sysPostCnv.poToVo(po));
        }
        return voList;
    }

    /**
     * 查询所有岗位
     * 
     * @return 岗位列表
     */
    @Override
    public List<SysPostVO> selectPostAll()
    {
        List<SysPostPO> list=postMapper.selectList(null);
        List<SysPostVO> voList=new ArrayList<>();
        for(SysPostPO po:list){
            voList.add(sysPostCnv.poToVo(po));
        }
        return voList;
    }

    /**
     * 通过岗位ID查询岗位信息
     * 
     * @param postId 岗位ID
     * @return 角色对象信息
     */
    @Override
    public SysPostVO selectPostById(Long postId)
    {
        SysPostPO po=postMapper.selectById(postId);
        return sysPostCnv.poToVo(po);
    }

    /**
     * 根据用户ID获取岗位选择框列表
     * 
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    @Override
    public List<Long> selectPostListByUserId(Long userId)
    {
        List<Long> list=postMapper.selectPostListByUserId(userId);
        return list;
    }

    /**
     * 校验岗位名称是否唯一
     * 
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostNameUnique(SysPostDTO post)
    {
        Long postId = StringUtils.isNull(post.getPostId()) ? -1L : post.getPostId();
        List<SysPostPO> list=postMapper.selectList(new QueryWrapper<SysPostPO>().eq("post_name",post.getPostName()));
        if (list.isEmpty()){
            return UserConstants.UNIQUE;
        }
        SysPostPO info = list.get(0);
        if (StringUtils.isNotNull(info) && info.getPostId().longValue() != postId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验岗位编码是否唯一
     * 
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostCodeUnique(SysPostDTO post)
    {
        Long postId = StringUtils.isNull(post.getPostId()) ? -1L : post.getPostId();
        List<SysPostPO> list=postMapper.selectList(new QueryWrapper<SysPostPO>().eq("post_code",post.getPostCode()));
        if (list.isEmpty()){
            return UserConstants.UNIQUE;
        }
        SysPostPO info = list.get(0);
        if (StringUtils.isNotNull(info) && info.getPostId().longValue() != postId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 通过岗位ID查询岗位使用数量
     * 
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public int countUserPostById(Long postId)
    {
        return userPostMapper.countUserPostById(postId);
    }

    /**
     * 删除岗位信息
     * 
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public int deletePostById(Long postId)
    {
        return postMapper.deleteById(postId);
    }

    /**
     * 批量删除岗位信息
     * 
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    @Override
    public int deletePostByIds(Long[] postIds)
    {
        for (Long postId : postIds)
        {
            SysPostVO post = selectPostById(postId);
            if (countUserPostById(postId) > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能删除", post.getPostName()));
            }
        }
        return postMapper.deletePostByIds(postIds);
    }

    /**
     * 新增保存岗位信息
     * 
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public int insertPost(SysPostDTO post)
    {

        return postMapper.insert(sysPostCnv.dtoToPo(post));
    }

    /**
     * 修改保存岗位信息
     * 
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public int updatePost(SysPostDTO post)
    {
        return postMapper.updateById(sysPostCnv.dtoToPo(post));
    }
}
