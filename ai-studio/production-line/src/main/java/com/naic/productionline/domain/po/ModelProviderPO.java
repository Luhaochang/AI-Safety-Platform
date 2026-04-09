package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 模型提供方表对象 model_provider
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("model_provider")
@ApiModel(value = "模型提供方表对象")
public class ModelProviderPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /* 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /* 场景名称 */
    private String providerName;

    /* 场景描述 */
    private String description;
}
