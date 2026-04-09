package com.naic.productionline.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 模型对象 model
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value = "模型对象DTO")
public class ModelDTOOld implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "模型名称")
    private String name;

    @ApiModelProperty(value = "模型描述")
    private String description;

    @ApiModelProperty(value = "模型文件url")
    private String fileUrl;

    @ApiModelProperty(value = "模型参数")
    private String paramsString;

    @ApiModelProperty(value = "模型作者")
    private String author;

    @ApiModelProperty(value = "configMap内容")
    private String configMap;

    @ApiModelProperty(value = "标签id信息")
    private List<Long> tagIdList;

    @ApiModelProperty(value = "readme")
    private String readme;

    @ApiModelProperty(value = "二级应用场景id")
    private Long secondaryScene;

    @ApiModelProperty(value = "模型输入输出说明")
    private String modelPut;

}
