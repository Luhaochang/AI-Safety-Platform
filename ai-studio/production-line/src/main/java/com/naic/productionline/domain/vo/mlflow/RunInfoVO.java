package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 任务显示对象VO run_info_vo
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="run属性对象VO")
public class RunInfoVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "run_id")
    @JsonProperty("run_id")
    private String runId;

    @ApiModelProperty(value = "run_uuid")
    @JsonProperty("run_uuid")
    private String runUuid;

    @ApiModelProperty(value = "实验名称")
    @JsonProperty("experiment_id")
    private String experimentId;

    @ApiModelProperty(value = "run名称")
    @JsonProperty("run_name")
    private String runName;

    @ApiModelProperty(value = "timestamp")
    @JsonProperty("artifact_location")
    private Long timestamp;

    @ApiModelProperty(value = "实验状态")
    @JsonProperty("status")
    private String status;

    @ApiModelProperty(value = "开始时间")
    @JsonProperty("start_time")
    private Long startTime;

    @ApiModelProperty(value = "结束时间")
    @JsonProperty("end_time")
    private Long endTime;

    @ApiModelProperty(value = "artifact_uri")
    @JsonProperty("artifact_uri")
    private String artifactUri;

    @ApiModelProperty(value = "lifecycle_stage")
    @JsonProperty("lifecycle_stage")
    private String lifecycleStage;

}
