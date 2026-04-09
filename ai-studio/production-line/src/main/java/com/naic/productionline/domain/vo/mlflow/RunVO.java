package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

/**
 * 任务对象VO run_vo
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="run对象VO")
public class RunVO {

    private static final long serialVersionUID = 1L;

    @JsonProperty("info")
    private RunInfoVO info;

    @JsonProperty("data")
    private RunDataVO data;

    @JsonProperty("outputs")
    private OutputsVO outputs;
}
