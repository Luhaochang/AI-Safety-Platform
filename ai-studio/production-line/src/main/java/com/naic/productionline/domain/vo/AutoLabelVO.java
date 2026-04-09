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
 * 自动标注任务表对象 data_set_task_vo
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="自动标注任务表对象VO")
public class AutoLabelVO extends BaseEntityVO
{

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "自增id")
    private Long id;

    @ApiModelProperty(value = "自动标注名称")
    private String name;

    @ApiModelProperty(value = "服务id")
    private Long serviceId;

    @ApiModelProperty(value = "服务名")
    private String serviceName;

    @ApiModelProperty(value = "数据集标注任务id")
    private Long dataSetTaskId;

    @ApiModelProperty(value = "标注状态：0.未标注1.排队中2.标注中3.已标注4.标注失败")
    private Integer status;
}
