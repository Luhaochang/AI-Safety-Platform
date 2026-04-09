package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.PipelineTaskPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型产线管理 数据层
 *
 * @author wangyunong
 */
@Mapper
public interface PipelineTaskMapper extends BaseMapper<PipelineTaskPO> {

}