package com.naic.productionline.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.naic.api.entity.vo.BaseEntityVO;
import com.naic.productionline.domain.po.JupyterImage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 模型服务对象  jupyter_service_vo
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value=" Jupyter服务对象VO")
public class JupyterServiceVO
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "自增id")
    private Long id;

    @ApiModelProperty(value = "服务名称")
    private String name;

    @ApiModelProperty(value = "jupyter id")
    private Long jupyterId;

    @ApiModelProperty(value = "模型")
    private JupyterImage jupyterImage;

    @ApiModelProperty(value = "服务运行名称")
    private String serviceName;

    @ApiModelProperty(value = "服务url")
    private String serviceUrl;

    @ApiModelProperty(value = "部署状态：1.部署中2.部署完成3.部署失败4.暂停")
    private Integer status;

    @ApiModelProperty(value = "模型id")
    private Long modelId;
}
