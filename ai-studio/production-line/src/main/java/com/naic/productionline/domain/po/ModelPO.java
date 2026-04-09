package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.naic.api.entity.po.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 模型对象 model
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("model") // 修改表名
@ApiModel(value = "模型对象")
public class ModelPO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 应用场景 (对应 app_scene_id)
     */
    private String appSceneId;

    /**
     * 任务类型id (对应 task_type_id)
     */
    private Long taskTypeId;

    /**
     * 提供方id (对应 provider_id)
     */
    private Long providerId;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 模型框架id (对应 frame_id)
     */
    private Long frameId;

    /**
     * 模型尺寸 (对应 model_size)
     */
    private Long modelSize;

    /**
     * 模型评分 (对应 model_score)
     */
    private BigDecimal modelScore;

    /**
     * 模型浏览量 (对应 model_views)
     */
    private Long modelViews;

    /**
     * 模型训练镜像url
     */
    private String fileUrl;

    /**
     * 模型url
     */
    private String modelUrl;

    /**
     * 模型服务url
     */
    private String serviceImage;

    /**
     * 模型参数
     */
    private String paramsString;

    /**
     * 模型作者
     */
    private String author;

    /**
     * 是否官方模型：0.否 1.是
     */
    private Integer isOfficial;

    /**
     * config map
     */
    private String configMap;

    /**
     * readme
     */
    private String readme;

    /**
     * 模型输入输出说明
     */
    private String modelPut;

    /**
     * 模型结果类别字典
     */
    private String classDict;

}
