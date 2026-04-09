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
 * 模型产线表对象 pipeline_task_vo
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value = "模型产线表对象VO")
public class PipelineTaskVO extends BaseEntityVO {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "自增id")
    private Long id;

    @ApiModelProperty(value = "产线名称")
    private String lineName;

    @ApiModelProperty(value = "产线ID")
    private String lineId;

    @ApiModelProperty(value = "应用场景id")
    private String appSceneId;

    @ApiModelProperty(value = "任务类型：1. 图片分类 2. 目标检测 ...")
    private Long taskTypeId;

    @ApiModelProperty(value = "模型id")
    private Long modelId;

    @ApiModelProperty(value = "模型name")
    private String modelName;

    @ApiModelProperty(value = "数据集id")
    private Long dataSetId;

    @ApiModelProperty(value = "数据集name")
    private String dataSetName;

    @ApiModelProperty(value = "参数准备字符串")
    private String paramsString;

    @ApiModelProperty(value = "前端画布编排快照")
    private String canvasSnapshot;

    @ApiModelProperty(value = "产线状态：0.配置中1.排队中2.运行中3.已停止4.运行完成5.运行失败")
    private Integer status;

    @ApiModelProperty(value = "镜像导出状态0.未导出1.导出中2.导出完成3.导出失败")
    private Integer isPackage;

    @ApiModelProperty(value = "镜像url")
    private String imageUrl;

    @ApiModelProperty(value = "模型训练完成所用时间,单位：毫秒")
    private Long trainTime;
}