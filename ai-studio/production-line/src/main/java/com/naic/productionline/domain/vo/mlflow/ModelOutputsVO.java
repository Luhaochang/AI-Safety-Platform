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
@ApiModel(value="ModelOutputsVO")
public class ModelOutputsVO {

    @ApiModelProperty(value = "model_id")
    @JsonProperty("model_id")
    private String modelId;

    @ApiModelProperty(value = "step")
    @JsonProperty("step")
    private Integer step;

}
