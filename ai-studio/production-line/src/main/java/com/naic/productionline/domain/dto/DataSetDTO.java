package com.naic.productionline.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 数据集表 data_set DTO
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value = "数据集表对象DTO")
public class DataSetDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "数据集名称")
    private String dataSetName;

    @ApiModelProperty(value = "应用场景ID")
    private String appSceneId;

    @ApiModelProperty(value = "任务类型ID")
    private Long taskTypeId;

    @ApiModelProperty(value = "简介摘要")
    private String dataSetAbstract;

    @ApiModelProperty(value = "数据类型")
    private Integer dataType;

    @ApiModelProperty(value = "标签")
    private String tag;

    @ApiModelProperty(value = "开源协议")
    private Integer agreement;

    @ApiModelProperty(value = "数据集作者")
    private String dateAuthor;

    @ApiModelProperty(value = "是否公开：0.否 1.是")
    private Integer isPublic;

    @ApiModelProperty(value = "是否标注：0.未标注1. 标注中2.已标注")
    private Integer isMarked;

}