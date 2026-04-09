package com.naic.productionline.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.naic.api.entity.vo.BaseEntityVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 模型提供方表对象 model_provider
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value = "模型提供方表对象")
public class ModelProviderVO extends BaseEntityVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "提供方名称")
    private String providerName;

    @ApiModelProperty(value = "提供方简介")
    private String description;
}
