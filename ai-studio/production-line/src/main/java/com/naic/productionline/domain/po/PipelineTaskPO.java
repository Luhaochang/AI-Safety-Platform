package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.naic.api.entity.po.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 模型产线表对象 pipeline_task
 *
 * @author xingdong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("pipeline_task")
@ApiModel(value = "模型产线表对象")
public class PipelineTaskPO extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 产线名称
     */
    private String lineName;

    /**
     * 产线ID
     */
    private String lineId;

    /**
     * 应用场景：1. 故障缺陷诊断 2. 表计自动读取 ...
     */
    private String appSceneId;

    /**
     * 任务类型：1. 图片分类 2. 目标检测 3. 图片分割 4. 目标跟踪 5. OCR文字识别 6. OBB旋转检测 7. 时序预测 8. 异常检测 9. 知识库问答 10.信息抽取 11. 内容生成
     */
    private Long taskTypeId;

    /**
     * 模型id
     */
    private Long modelId;

    /**
     * 数据集id
     */
    private Long dataSetId;

    /**
     * 参数准备字符串
     */
    private String paramsString;

    /**
     * 前端画布编排快照 (UI还原用)
     */
    @TableField(value = "canvas_snapshot")
    private String canvasSnapshot;

    /**
     * 产线状态：0.配置中1.排队中2.运行中3.已停止4.运行完成5.运行失败
     */
    private Integer status;

    /**
     * job 名称
     */
    private String jobName;

    /**
     * configMap 名称
     */
    private String mapName;

    /**
     * job 命名空间
     */
    private String nameSpace;

    /**
     * 实验名称
     */
    private String experimentName;

    /**
     * 镜像导出状态0.未导出1.导出中2.导出完成3.导出失败
     */
    private Integer isPackage;

    /**
     * 镜像url
     */
    private String imageUrl;

    /**
     * 模型训练完成所用时间，单位：毫秒
     */
    private Long trainTime;

    /**
     * 训练的epoch次数
     */
    private Long epochs;

}