package com.naic.productionline.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 任务类型表对象 task_type
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="任务类型表对象")
public class TaskTypeVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "任务代码")
    private String taskCode;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务模态")
    private String modality;

    @ApiModelProperty(value = "任务描述")
    private String description;

}
