package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.ClusterNodePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClusterNodeMapper extends BaseMapper<ClusterNodePO> {
}
