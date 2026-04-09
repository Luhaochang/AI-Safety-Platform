package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Metric结果对象VO metric_response
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="Metric结果对象VO")
public class MetricResponse {

    private static final long serialVersionUID = 1L;

    @JsonProperty("metrics")
    private List<MetricVO> metrics;

}
