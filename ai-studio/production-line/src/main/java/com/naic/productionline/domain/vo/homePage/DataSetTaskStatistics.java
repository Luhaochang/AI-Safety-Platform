package com.naic.productionline.domain.vo.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 数据集标注任务情况统计 data_set_task_statistics
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="数据集标注任务情况统计")
public class DataSetTaskStatistics {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "标注任务名称")
    private String dataSetTaskName;

    @ApiModelProperty(value = "标注完成进度")
    private Double completionProgress;


}
