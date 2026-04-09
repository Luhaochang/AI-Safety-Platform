package com.naic.productionline.domain.vo.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 节点情况统计 node_statistics
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="节点情况统计")
public class NodeStatistics {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "节点名称")
    private String nodeName;

    @ApiModelProperty(value = "节点地址")
    private String nodeAddress;

    @ApiModelProperty(value = "可分配cpu核心总数")
    private BigDecimal cpuNumber;

    @ApiModelProperty(value = "可分配内存总数")
    private BigDecimal memoryNumber;

    @ApiModelProperty(value = "可分配NVIDIA GPU资源数量")
    private BigDecimal gpuNumber;

    @ApiModelProperty(value = "可分配Pod数量")
    private BigDecimal podsNumber;

    @ApiModelProperty(value = "GPU使用率")
    private String gpuUtilization;

    @ApiModelProperty(value = "CPU使用率")
    private String cpuUtilization;

    @ApiModelProperty(value = "内存使用率")
    private String memoryUtilization;

    @ApiModelProperty(value = "文件系统使用率")
    private String fileSystemUtilization;
}
