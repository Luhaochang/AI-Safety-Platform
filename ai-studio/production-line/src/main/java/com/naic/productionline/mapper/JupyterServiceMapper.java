package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.JupyterImage;
import com.naic.productionline.domain.po.JupyterServicePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * JupyterImage表对象 数据层
 * 
 * @author wangyunong
 */
@Mapper
public interface JupyterServiceMapper extends BaseMapper<JupyterServicePO> {

}
