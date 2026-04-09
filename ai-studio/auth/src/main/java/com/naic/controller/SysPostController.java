package com.naic.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import com.naic.domain.entity.dto.SysPostDTO;
import com.naic.domain.entity.vo.SysPostVO;
import com.naic.log.annotation.Log;
import com.naic.service.ISysPostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * 岗位信息操作处理
 * 
 * @author wangyunong
 */
@Api(tags = "岗位信息操作处理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/system/post")
public class SysPostController
{

    private final ISysPostService postService;

    @PreAuthorize("@ss.hasPermi('system:post:list')")
    @PostMapping("/list")
    @ApiOperation("获取岗位列表")
    @Log()
    public Result<DataPage<SysPostVO>> list(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo, @RequestParam Integer pageSize)
    {
        List<SysPostVO> list = postService.selectPostList(condition);
        return Result.success(DataPage.rest(list,pageNo,pageSize));
    }

    @PreAuthorize("@ss.hasPermi('system:post:query')")
    @GetMapping(value = "/{postId}")
    @ApiOperation("根据岗位编号获取详细信息")
    @Log()
    public Result<SysPostVO> getInfo(@PathVariable Long postId)
    {
        return Result.success(postService.selectPostById(postId));
    }

    @PreAuthorize("@ss.hasPermi('system:post:add')")
    @PostMapping
    @ApiOperation("新增岗位")
    @Log()
    public Result<?> add(@Validated @RequestBody SysPostDTO post)
    {
        if (!postService.checkPostNameUnique(post))
        {
            throw new ServiceException("新增岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        }
        else if (!postService.checkPostCodeUnique(post))
        {
            throw new ServiceException("新增岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        postService.insertPost(post);
        return Result.success("新增岗位成功");
    }

    @PreAuthorize("@ss.hasPermi('system:post:edit')")
    @PutMapping
    @ApiOperation("修改岗位")
    @Log()
    public Result<?> edit(@Validated @RequestBody SysPostDTO post)
    {
        if (!postService.checkPostNameUnique(post))
        {
            throw new ServiceException("修改岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        }
        else if (!postService.checkPostCodeUnique(post))
        {
            throw new ServiceException("修改岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        postService.updatePost(post);
        return Result.success("修改岗位成功");
    }

    @PreAuthorize("@ss.hasPermi('system:post:remove')")
    @DeleteMapping("/{postIds}")
    @ApiOperation("删除岗位")
    @Log()
    public Result<?> remove(@PathVariable Long[] postIds)
    {
        postService.deletePostByIds(postIds);
        return Result.success("删除岗位成功");
    }

    @GetMapping("/option-select")
    @ApiOperation("获取岗位选择框列表")
    @Log()
    public Result<List<SysPostVO>> optionselect()
    {
        List<SysPostVO> posts = postService.selectPostAll();
        return Result.success(posts);
    }
}
