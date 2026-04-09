package com.naic.productionline.domain.vo.homePage;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 集群节点情况统计 node_metric
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="集群metric")
public class NodeMetric {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "是否文件夹")
    @JsonProperty("__name__")
    private String name;

    @ApiModelProperty(value = "hostname")
    private String hostname;

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "instance")
    private String instance;

    @ApiModelProperty(value = "job")
    private String job;

    @ApiModelProperty(value = "job")
    private String type;

    @ApiModelProperty(value = "job")
    private String uuid;
}
