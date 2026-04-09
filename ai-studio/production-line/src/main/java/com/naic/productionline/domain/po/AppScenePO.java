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
 * 应用场景表对象 app_scene
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("app_scene")
@ApiModel(value="应用场景表对象")
public class AppScenePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /* 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /* 场景代码 */
    private String appCode;

    /* 场景名称 */
    private String appName;

    /* 行业名称 */
    private String industry;

    /* 关联任务场景id */
    private String relatedTasksId;

    /* 场景描述 */
    private String description;
}
