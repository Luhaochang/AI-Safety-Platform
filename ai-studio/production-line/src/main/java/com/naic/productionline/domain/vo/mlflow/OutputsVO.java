package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="OutputsVO")
public class OutputsVO {
    private static final long serialVersionUID = 1L;

    @JsonProperty("model_outputs")
    private List<ModelOutputsVO> modelOutputsVOS;
}
