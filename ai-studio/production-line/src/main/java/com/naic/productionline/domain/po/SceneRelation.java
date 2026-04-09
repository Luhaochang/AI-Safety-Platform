package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 应用场景与任务场景关联表对象 data_set
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("scene_relation")
@ApiModel(value="应用场景与任务场景关联表对象")
public class SceneRelation
{
    private static final long serialVersionUID = 1L;

    /** 次级应用场景自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 应用场景：1.工业 2.农业 3.纺织业 ...... */
    private Integer applicationScene;

    /** 次级场景名称 */
    private String secondarySceneName;

    /** 关联的任务场景:0.图片分类1.目标检测2.目标跟踪3.图片分割4.OCR */
    private Integer sceneId;

    /** 场景描述 */
    private String description;

}
