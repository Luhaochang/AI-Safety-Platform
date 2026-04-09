package com.naic.productionline.domain.vo.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 模型训练情况统计 pipeline_task_statistics
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="模型训练情况统计")
public class PipelineTaskStatistics {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "模型训练任务名称")
    private String pipelineTaskName;

    @ApiModelProperty(value = "模型训练完成进度")
    private Double completionProgress;


}
