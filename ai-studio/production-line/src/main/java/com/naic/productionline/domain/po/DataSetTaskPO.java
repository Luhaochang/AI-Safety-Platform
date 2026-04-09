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
 * 标注任务表对象 data_set_task
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("data_set_task")
@ApiModel(value="标注任务表对象")
public class DataSetTaskPO extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 数据集id */
    private Long dataSetId;

    /** 任务名称 */
    private String taskName;

    /** 任务描述 */
    private String taskDescription;

    /** 标签值字符串 */
    private String label;

    /** 数据类型1: Image, 2: Text, 3: Table */
    private Integer dataType;

    /** 状态：0.标注中1.标注完成 */
    private Integer status;

    /** 完成数量 */
    private Long finishNumber;

    /** 标注总数 */
    private Long setNumber;

}
