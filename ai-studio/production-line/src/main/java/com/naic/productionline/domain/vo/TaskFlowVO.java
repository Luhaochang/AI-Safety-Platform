package com.naic.productionline.domain.vo;

import com.naic.productionline.domain.vo.mlflow.MetricVO;
import com.naic.productionline.domain.vo.mlflow.ParametersVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 任务显示对象VO task_flow_vo
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="任务显示对象VO")
public class TaskFlowVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "run_id")
    private String runId;

    @ApiModelProperty(value = "run_uuid")
    private String runUuId;

    @ApiModelProperty(value = "实验状态")
    private String status;

    @ApiModelProperty(value = "生命周期阶段")
    private String lifecycleStage;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "运行时间")
    private String timeDiff;

    @ApiModelProperty(value = "params_list")
    private List<ParametersVO> paramsList;

    @ApiModelProperty(value = "metric_list")
    private Map<String, List<MetricVO>> metricList;
}
