package com.naic.productionline.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 模型标签对象 model_tag
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="模型标签对象DTO")
public class ModelTagDTO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "类别：1.系统支持 2.硬件支持")
    private Integer category;

    @ApiModelProperty(value = "标签名称")
    private String tagName;

    @ApiModelProperty(value = "icon")
    private String icon;

    @ApiModelProperty(value = "是否是一级标签是否是一级标签:0.二级标签1.一级标签")
    private Integer isSuper;

    @ApiModelProperty(value = "上级标签，一级标签的上级标签为0")
    private Long parentId;

}
