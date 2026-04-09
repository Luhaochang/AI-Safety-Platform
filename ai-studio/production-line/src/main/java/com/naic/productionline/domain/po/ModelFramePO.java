package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 模型框架表对象 model_frame
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("model_frame")
@ApiModel(value = "模型框架表对象")
public class ModelFramePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /* 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /* 框架名称 */
    private String frameName;

    /* 框架描述 */
    private String description;
}
