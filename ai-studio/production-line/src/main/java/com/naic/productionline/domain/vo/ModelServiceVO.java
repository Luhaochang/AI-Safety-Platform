package com.naic.productionline.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.naic.api.entity.vo.BaseEntityVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 模型服务对象 model_service_vo
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="服务对象VO")
public class ModelServiceVO extends BaseEntityVO
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "自增id")
    private Long id;

    @ApiModelProperty(value = "服务名称")
    private String name;

    @ApiModelProperty(value = "服务描述")
    private String description;

    @ApiModelProperty(value = "readme文件")
    private String readme;

    @ApiModelProperty(value = "模型")
    private ModelVO model;

    @ApiModelProperty(value = "服务url")
    private String serviceUrl;

    @ApiModelProperty(value = "部署状态：1.部署中2.部署完成3.部署失败4.暂停")
    private Integer status;


}
