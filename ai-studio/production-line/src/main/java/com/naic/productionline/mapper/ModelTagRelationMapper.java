package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.ModelTagRelationPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型标签关系管理 数据层
 * 
 * @author wangyunong
 */
@Mapper
public interface ModelTagRelationMapper extends BaseMapper<ModelTagRelationPO> {

}
