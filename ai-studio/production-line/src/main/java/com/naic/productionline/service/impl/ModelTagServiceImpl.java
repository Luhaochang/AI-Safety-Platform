package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.api.api.DataPage;
import com.naic.api.api.ServiceException;
import com.naic.api.utils.Condition;
import com.naic.productionline.domain.cnv.ModelTagCnv;
import com.naic.productionline.domain.po.ModelTagPO;
import com.naic.productionline.domain.vo.ModelTagVO;
import com.naic.productionline.mapper.ModelTagMapper;
import com.naic.productionline.service.ModelTagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模型标签管理 服务实现
 *
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class ModelTagServiceImpl extends ServiceImpl<ModelTagMapper, ModelTagPO> implements ModelTagService {

    private final ModelTagMapper modelTagMapper;

    private final ModelTagCnv modelTagCnv;
    @Override
    public List<ModelTagVO> getModelTagByCategory(Integer category) {
        //类别：1.系统支持 2.硬件支持
        if((category!=1)&&(category!=2)){
            throw new ServiceException("类别错误");
        }
        List<ModelTagPO> tagList=new ArrayList<>();
        if(category.equals(1)) {
            tagList = modelTagMapper.selectList(new QueryWrapper<ModelTagPO>().eq("category", category));
        }
        else {
            tagList = modelTagMapper.selectList(new QueryWrapper<ModelTagPO>().eq("category", category).eq("is_super",1));
        }
        return convertList(tagList);
    }

    @Override
    public List<ModelTagVO> getModelTagBySuper(Long superId) {
        List<ModelTagPO> tagList=modelTagMapper.selectList(new QueryWrapper<ModelTagPO>().eq("parent_id",superId));
        return convertList(tagList);
    }

    @Override
    public DataPage<ModelTagVO> selectModel(Map<String, Object> condition, Integer pageNo, Integer pageSize) {
        IPage<ModelTagVO> page=modelTagMapper.selectPage(new Page<>(pageNo,pageSize), Condition.getQueryWrapper(condition,ModelTagPO.class)).convert(modelTagCnv::poToVo);
        return DataPage.rest(page);
    }

    public List<ModelTagVO> convertList(List<ModelTagPO> poList){
        List<ModelTagVO> voList=new ArrayList<>();
        for(ModelTagPO tag:poList){
            ModelTagVO vo=modelTagCnv.poToVo(tag);
            voList.add(vo);
        }
        return voList;
    }
}
