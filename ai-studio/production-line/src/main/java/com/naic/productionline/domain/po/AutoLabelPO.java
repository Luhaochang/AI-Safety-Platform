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
 * 自动标注表对象 auto_label
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("auto_label")
@ApiModel(value="自动标注任务表对象")
public class AutoLabelPO extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 自动标注名称 */
    private String name;

    /** 服务id */
    private Long serviceId;

    /** 数据集标注任务id */
    private Long dataSetTaskId;

    /** 标注状态：0.未标注1.排队中2.标注中3.已标注4.标注失败 */
    private Integer status;

    /** job名称 */
    private String jobName;

    /**configMap 名称*/
    private String mapName;

    /**job 命名空间*/
    private String nameSpace;

}
