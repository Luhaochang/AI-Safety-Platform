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
 * 标注任务表对象 data_set_task_vo
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="标注任务表对象VO")
public class DataSetTaskVO extends BaseEntityVO
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "自增id")
    private Long id;

    @ApiModelProperty(value = "数据集id")
    private Long dataSetId;

    @ApiModelProperty(value = "数据集名称")
    private String dataSetName;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务描述")
    private String taskDescription;

    @ApiModelProperty(value = "标签值字符串")
    private String label;

    @ApiModelProperty(value = "数据类型1: Image, 2: Text, 3: Table")
    private Integer dataType;

    @ApiModelProperty(value = "状态：0.标注中1.标注完成")
    private Integer status;

    @ApiModelProperty(value = "完成数量")
    private Long finishNumber;

    @ApiModelProperty(value = "标注总数")
    private Long setNumber;

}
