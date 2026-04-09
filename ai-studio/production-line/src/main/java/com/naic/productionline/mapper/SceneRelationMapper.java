package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.SceneRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用场景与任务场景关联表对象 数据层
 * 
 * @author wangyunong
 */
@Mapper
public interface SceneRelationMapper extends BaseMapper<SceneRelation> {

}
