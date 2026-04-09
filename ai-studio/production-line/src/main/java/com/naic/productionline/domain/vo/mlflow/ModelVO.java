package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="ModelVO")
public class ModelVO {

    private static final long serialVersionUID = 1L;

    @JsonProperty("info")
    private InfoVO info;
}
