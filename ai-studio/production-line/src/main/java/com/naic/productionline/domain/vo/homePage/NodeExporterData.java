package com.naic.productionline.domain.vo.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 集群节点情况数据实体 node_exporter_data
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="集群节点情况数据实体")
public class NodeExporterData {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "resultType")
    private String resultType;

    @ApiModelProperty(value = "value")
    private List<NodeExporterStatistics> result;

}
