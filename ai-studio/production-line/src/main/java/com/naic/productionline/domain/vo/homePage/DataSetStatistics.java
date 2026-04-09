package com.naic.productionline.domain.vo.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 数据集情况统计 data_set_statistics
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value = "数据集情况统计")
public class DataSetStatistics {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务类型：1. 图片分类 2. 目标检测 3. 图片分割 4. 目标跟踪 5. OCR文字识别 6. OBB旋转检测 7. 时序预测 8. 异常检测 9. 知识库问答 10.信息抽取 11. 内容生成")
    private Long taskTypeId;

    @ApiModelProperty(value = "数据集数量")
    private Integer dataSetNum;


}
