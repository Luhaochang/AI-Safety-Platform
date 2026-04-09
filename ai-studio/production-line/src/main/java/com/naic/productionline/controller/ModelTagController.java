package com.naic.productionline.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.cnv.ModelTagCnv;
import com.naic.productionline.domain.dto.ModelTagDTO;
import com.naic.productionline.domain.po.ModelTagPO;
import com.naic.productionline.domain.vo.ModelTagVO;
import com.naic.productionline.service.ModelTagRelationService;
import com.naic.productionline.service.ModelTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * @author wyn
 */
@Api(tags = "模型标签管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/model-tag")
public class ModelTagController {

    private final ModelTagService modelTagService;

    private final ModelTagCnv modelTagCnv;

    private final ModelTagRelationService modelTagRelationService;

    @GetMapping("/condtion")
    @ApiOperation("分页查询")
    @Log()
    public Result<DataPage<ModelTagVO>> getCondition(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo, @RequestParam Integer pageSize){
        return Result.success(modelTagService.selectModel(condition,pageNo,pageSize));
    }

    @GetMapping("/by-category")
    @ApiOperation("按类别查询")
    @Log()
    public Result<List<ModelTagVO>> getTagByCategory(@RequestParam Integer category){
        return Result.success(modelTagService.getModelTagByCategory(category));
    }

    @GetMapping("/by-super")
    @ApiOperation("获取子标签")
    @Log()
    public Result<List<ModelTagVO>> getTagBySuper(@RequestParam Long superId){
        return Result.success(modelTagService.getModelTagBySuper(superId));
    }

    @GetMapping("by-id")
    @ApiOperation("根据id查询标签")
    @Log()
    public Result<ModelTagVO> getModelById(@RequestParam Long id){
        return Result.success(modelTagCnv.poToVo(modelTagService.getById(id)));
    }

    @PostMapping
    @ApiOperation("新增标签")
    @Log()
    public Result<?> insertModelTag(@RequestBody ModelTagDTO dto) {
        if((dto.getCategory()!=1)&&(dto.getCategory()!=2)){
            throw new ServiceException("标签类别错误");
        }

        if(dto.getCategory().equals(1)){
            dto.setIsSuper(0);
            dto.setParentId(0L);
        }
        else {
            if (dto.getIsSuper().equals(1)) {
                dto.setParentId(0L);
            }
            else {
                if(modelTagService.getOne(new QueryWrapper<ModelTagPO>().eq("id",dto.getParentId()))==null){
                    throw new ServiceException("没有该一级标签");
                }
            }
        }
        modelTagService.save(modelTagCnv.dtoToPo(dto));
        return Result.success("新增成功");
    }

    @PutMapping
    @ApiOperation("编辑标签")
    @Log()
    public Result<?> updateModelTag(@RequestBody ModelTagDTO dto) {
        if((dto.getCategory()!=1)&&(dto.getCategory()!=2)){
            throw new ServiceException("标签类别错误");
        }
        ModelTagPO tag=modelTagService.getById(dto.getId());
        if(modelTagRelationService.isHasRelation(tag.getId())){
            throw new ServiceException("该标签已经被使用，无法编辑");
        }
        if(dto.getCategory().equals(1)){
            dto.setIsSuper(0);
            dto.setParentId(0L);
        }
        else {
            if (dto.getIsSuper().equals(1)) {
                dto.setParentId(0L);
            }
            else {
                if(modelTagService.getOne(new QueryWrapper<ModelTagPO>().eq("id",dto.getParentId()))==null){
                    throw new ServiceException("没有该一级标签");
                }
            }
        }
        modelTagService.updateById(modelTagCnv.dtoToPo(dto));
        return Result.success("编辑成功");
    }

    @DeleteMapping
    @ApiOperation("删除标签")
    @Log()
    public Result<?> deleteModelTag(@RequestParam Long id){
        ModelTagPO tag=modelTagService.getById(id);
        if (tag==null){
            return Result.success("删除成功");
        }
        if(modelTagRelationService.isHasRelation(tag.getId())){
            throw new ServiceException("该标签已经被使用，无法删除");
        }
        if(tag.getIsSuper().equals(1)){
            List<ModelTagPO> list=modelTagService.list(new QueryWrapper<ModelTagPO>().eq("parent_id",id));
            if (list.size()>0){
                throw new ServiceException("该标签为一级标签，且存在子标签，无法删除");
            }
        }
        modelTagService.removeById(id);
        return Result.success("删除成功");
    }
}
