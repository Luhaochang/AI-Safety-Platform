package com.naic.productionline.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 模型产线表对象 pipeline_task_dto
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value = "模型产线表对象DTO")
public class PipelineTaskDTO {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "自增id")
    private Long id;

    @ApiModelProperty(value = "产线名称")
    private String lineName;

    @ApiModelProperty(value = "应用场景ID")
    private String appSceneId;

    @ApiModelProperty(value = "任务类型：1. 图片分类 2. 目标检测 ...")
    private Long taskTypeId;

    @ApiModelProperty(value = "模型id")
    private Long modelId;

    @ApiModelProperty(value = "数据集id")
    private Long dataSetId;

    @ApiModelProperty(value = "参数准备字符串")
    private String paramsString;

    @ApiModelProperty(value = "前端画布编排快照")
    private String canvasSnapshot;
}