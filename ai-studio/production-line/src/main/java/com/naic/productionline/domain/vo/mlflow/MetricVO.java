package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * metric对象VO MetricVO
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="metric对象VO")
public class MetricVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "value")
    @JsonProperty("value")
    private Double value;

    @ApiModelProperty(value = "step")
    @JsonProperty("step")
    private Long step;

    @ApiModelProperty(value = "key")
    @JsonProperty("key")
    private String key;

    @ApiModelProperty(value = "timestamp")
    @JsonProperty("timestamp")
    private Long timestamp;
}
