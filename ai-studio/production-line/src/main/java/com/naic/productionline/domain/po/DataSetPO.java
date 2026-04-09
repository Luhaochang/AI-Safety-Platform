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
 * 数据集表对象 data_set
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("data_set")
@ApiModel(value = "数据集表对象")
public class DataSetPO extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 数据集名称
     */
    private String dataSetName;

    /**
     * 应用场景 (原 secondaryScene)
     */
    private String appSceneId;

    /**
     * 任务类型：1. 图片分类 2. 目标检测 ... (原 scene)
     */
    private Long taskTypeId;

    /**
     * 简介摘要
     */
    private String dataSetAbstract;

    /**
     * 数据类型: 1-图片, 2-文本, 3-表格行
     */
    private Integer dataType;

    /**
     * 数据集文件url
     */
    private String fileUrl;

    /**
     * 标签
     */
    private String tag;

    /**
     * 开源协议
     */
    private Integer agreement;

    /**
     * 数据集作者
     */
    private String dateAuthor;

    /**
     * 是否公开：0.否 1.是
     */
    private Integer isPublic;

    /**
     * 是否标注：0.未标注1. 标注中2.已标注
     */
    private Integer isMarked;

    /**
     * 是否已校验：0.未校验1.已校验
     */
    private Integer isChecked;

    /**
     * 是否官方数据集：0.否 1.是
     */
    private Integer isOfficial;

    /**
     * 是否已打包：0.否 1.是 2.打包中
     */
    private Integer isPackage;

}