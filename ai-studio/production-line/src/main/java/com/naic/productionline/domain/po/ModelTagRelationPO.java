package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 模型标签关系对象 model_tag_relation
 *
 * @author wangyunong
 */
@Data
@TableName("model_tag_relation")
@ApiModel(value="模型标签关系对象")
public class ModelTagRelationPO {

    private static final long serialVersionUID = 1L;

    /** 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 模型id */
    private Long modelId;

    /** 标签id */
    private Long tagId;

}
