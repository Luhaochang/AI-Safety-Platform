package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="InfoVO")
public class InfoVO {

    private static final long serialVersionUID = 1L;

    @JsonProperty("model_id")
    private String model_id;

    @JsonProperty("experiment_id")
    private String experiment_id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("creation_timestamp_ms")
    private Long creation_timestamp_ms;

    @JsonProperty("last_updated_timestamp_ms")
    private Long last_updated_timestamp_ms;

    @JsonProperty("artifact_uri")
    private String artifact_uri;

    @JsonProperty("status")
    private String status;

    @JsonProperty("model_type")
    private String model_type;

    @JsonProperty("source_run_id")
    private String source_run_id;
}
