package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.AutoLabelPO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 自动标注管理 数据层
 * 
 * @author wangyunong
 */
@Mapper
public interface AutoLabelMapper extends BaseMapper<AutoLabelPO> {

}
