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
 * 任务类型表对象 task_type
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("task_type")
@ApiModel(value="任务类型表对象")
public class TaskTypePO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 任务代码 */
    private String taskCode;

    /** 任务名称 */
    private String taskName;

    /** 模态 */
    private String modality;

    /** 描述 */
    private String description;

}
