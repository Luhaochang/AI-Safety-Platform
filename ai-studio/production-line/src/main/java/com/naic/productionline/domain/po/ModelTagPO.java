package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.naic.api.entity.po.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 模型标签对象 model_tag
 *
 * @author wangyunong
 */
@Data
@TableName("model_tag")
@ApiModel(value="模型标签对象")
public class ModelTagPO {

    private static final long serialVersionUID = 1L;

    /** 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 类别：1.系统支持 2.硬件支持 */
    private Integer category;

    /** 标签名称 */
    private String tagName;

    /** icon */
    private String icon;

    /** 是否是一级标签:0.二级标签1.一级标签 */
    private Integer isSuper;

    /** 上级标签，一级标签的上级标签为0 */
    private Long parentId;

}
