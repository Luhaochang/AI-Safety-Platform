package com.naic.productionline.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * MlflowRunDTO
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class MlflowRunDTO
{
    private static final long serialVersionUID = 1L;

    @JsonProperty("experiment_ids")
    List<String> experimentIds;

    @JsonProperty("max_results")
    Long maxResults;

    @JsonProperty("order_by")
    List<String> orderBy;

    @JsonProperty("run_view_type")
    String runViewType;
}
