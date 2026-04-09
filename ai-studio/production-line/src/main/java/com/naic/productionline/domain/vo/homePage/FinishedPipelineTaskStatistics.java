package com.naic.productionline.domain.vo.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 已完成模型训练任务的时间情况统计 finished_pipeline_task_statistics
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="已完成模型训练任务的时间情况统计")
public class FinishedPipelineTaskStatistics {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "时间分布：0. <1h 1. 1-24h 2.1-7day 3. 7-30day 4. >30day")
    private Integer type;

    @ApiModelProperty(value = "已完成模型训练任务数量")
    private Integer modelNum;


}
