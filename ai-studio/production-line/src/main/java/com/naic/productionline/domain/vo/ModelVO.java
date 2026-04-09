package com.naic.productionline.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.naic.api.entity.vo.BaseEntityVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 模型对象 VO
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "模型对象VO")
public class ModelVO extends BaseEntityVO {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "自增id")
    private Long id;

    @ApiModelProperty(value = "模型名称")
    private String name;

    @ApiModelProperty(value = "应用场景")
    private String appSceneId;

    @ApiModelProperty(value = "任务类型id")
    private Long taskTypeId;

    @ApiModelProperty(value = "提供方id")
    private Long providerId;

    @ApiModelProperty(value = "模型描述")
    private String description;

    @ApiModelProperty(value = "模型框架id")
    private Long frameId;

    @ApiModelProperty(value = "模型尺寸")
    private Long modelSize;

    @ApiModelProperty(value = "模型评分")
    private BigDecimal modelScore;

    @ApiModelProperty(value = "模型浏览量")
    private Long modelViews;

    @ApiModelProperty(value = "模型训练镜像url")
    private String fileUrl;

    @ApiModelProperty(value = "模型url")
    private String modelUrl;

    @ApiModelProperty(value = "服务镜像")
    private String serviceImage;

    @ApiModelProperty(value = "模型参数")
    private String paramsString;

    @ApiModelProperty(value = "模型作者")
    private String author;

    @ApiModelProperty(value = "是否官方数据集：0.否 1.是")
    private Integer isOfficial;

    @ApiModelProperty(value = "configMap内容")
    private String configMap;

    @ApiModelProperty(value = "readme")
    private String readme;

    @ApiModelProperty(value = "模型输入输出说明")
    private String modelPut;

    @ApiModelProperty(value = "模型结果类别字典")
    private String classDict;

    @ApiModelProperty(value = "标签列表")
    private List<ModelTagVO> tagList;
}