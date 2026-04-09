package com.naic.productionline.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 数据集表 AirFlowDTO
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class AirFlowDTO
{
    private static final long serialVersionUID = 1L;

    @JsonProperty("dag_run_id")
    private String dagRunId;

    private Map<String,Object> conf;

}
