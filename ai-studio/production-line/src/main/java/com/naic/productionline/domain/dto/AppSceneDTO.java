package com.naic.productionline.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 应用场景表对象 app_scene
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="应用场景表对象")
public class AppSceneDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "场景代码")
    private String appCode;

    @ApiModelProperty(value = "场景名称")
    private String appName;

    @ApiModelProperty(value = "行业名称")
    private String industry;

    @ApiModelProperty(value = "关联任务场景ID")
    private String relatedTasksId;

    @ApiModelProperty(value = "场景描述")
    private String description;
}
