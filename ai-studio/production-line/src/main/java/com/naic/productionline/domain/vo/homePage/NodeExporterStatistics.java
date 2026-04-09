package com.naic.productionline.domain.vo.homePage;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 集群节点情况统计 node_exporter_statistics
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="集群节点情况统计")
public class NodeExporterStatistics {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "metric")
    private NodeMetric metric;

    @ApiModelProperty(value = "value")
    private List<String> value;

}
