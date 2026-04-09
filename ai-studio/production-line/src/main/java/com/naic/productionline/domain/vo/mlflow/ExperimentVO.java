package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 实验对象VO experiment_vo
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="实验对象VO")
public class ExperimentVO {

    private static final long serialVersionUID = 1L;

    @JsonProperty("experiment_id")
    private String experimentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("artifact_location")
    private String artifactLocation;

    @JsonProperty("lifecycle_stage")
    private String lifecycleStage;

    @JsonProperty("last_update_time")
    private Long lastUpdateTime;

    @JsonProperty("creation_time")
    private Long creationTime;

}
