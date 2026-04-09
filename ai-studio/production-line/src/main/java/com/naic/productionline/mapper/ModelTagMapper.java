package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.ModelTagPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型标签管理 数据层
 * 
 * @author wangyunong
 */
@Mapper
public interface ModelTagMapper extends BaseMapper<ModelTagPO> {

}
