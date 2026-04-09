package com.naic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naic.productionline.domain.po.ModelPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 模型管理 数据层
 *
 * @author xingdong
 */
@Mapper
public interface ModelMapper extends BaseMapper<ModelPO> {

    /**
     * 浏览次数 +1
     * 使用 IFNULL 确保如果原值为 NULL 时能正确更新为 1
     */
    @Update("UPDATE model SET model_views = IFNULL(model_views, 0) + 1 WHERE id = #{id}")
    void incrementViews(@Param("id") Long id);
}