package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Experiment结果对象VO experiment_response
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="Experiment结果对象VO")
public class ExperimentResponse {

    private static final long serialVersionUID = 1L;

    @JsonProperty("experiments")
    private List<ExperimentVO> experiments;

}
