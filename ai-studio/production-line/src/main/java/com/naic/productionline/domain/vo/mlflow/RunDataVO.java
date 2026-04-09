package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * run_data对象VO
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="run_data对象VO")
public class RunDataVO {

    private static final long serialVersionUID = 1L;

    @JsonProperty("metrics")
    private List<MetricVO> metrics;

    @JsonProperty("params")
    private List<ParametersVO> params;
}
