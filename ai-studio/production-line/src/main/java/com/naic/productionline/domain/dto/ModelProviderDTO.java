package com.naic.productionline.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "模型提供方表对象")
public class ModelProviderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "提供方名称")
    private String providerName;

    @ApiModelProperty(value = "提供方简介")
    private String description;
}
