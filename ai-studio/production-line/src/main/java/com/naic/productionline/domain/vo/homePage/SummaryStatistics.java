package com.naic.productionline.domain.vo.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 总体情况统计 summary_statistics
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="总体情况统计")
public class SummaryStatistics {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "数据集总数")
    private Integer dataSetNum;

    @ApiModelProperty(value = "已标注数据集总数")
    private Integer markedDataSetNum;

    @ApiModelProperty(value = "模型总数")
    private Integer modelNum;

    @ApiModelProperty(value = "非官方模型总数")
    private Integer unofficialModelNum;

    @ApiModelProperty(value = "模型产线总数")
    private Integer pipelineTaskNum;

    @ApiModelProperty(value = "已完成的模型产线总数")
    private Integer finishedPipelineTaskNum;

    @ApiModelProperty(value = "服务总数")
    private Integer serviceNum;

    @ApiModelProperty(value = "运行中的服务总数")
    private Integer aliveServiceNum;

}
