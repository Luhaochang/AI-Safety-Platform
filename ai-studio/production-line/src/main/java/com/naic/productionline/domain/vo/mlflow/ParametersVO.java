package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 任务显示对象VO task_flow_vo
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="任务显示对象VO")
public class ParametersVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "key")
    @JsonProperty("key")
    private String key;

    @ApiModelProperty(value = "value")
    @JsonProperty("value")
    private String value;
}
