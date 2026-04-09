package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.ModelProviderPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型提供方管理 数据层
 *
 * @author xingdong
 */
@Mapper
public interface ModelProviderMapper extends BaseMapper<ModelProviderPO> {
}
