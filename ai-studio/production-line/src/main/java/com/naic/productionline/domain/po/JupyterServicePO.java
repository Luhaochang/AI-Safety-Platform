package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.naic.api.entity.po.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * jupyter对象 jupyter_service
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("jupyter_service")
@ApiModel(value="jupyter服务对象")
public class JupyterServicePO {

    private static final long serialVersionUID = 1L;

    /** 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 服务名称 */
    private String name;

    /** jupyter id */
    private Long jupyterId;

    /** 服务url */
    private String serviceUrl;

    /** 服务运行名称 */
    private String serviceName;

    /** 部署状态：1.部署中2.部署完成3.部署失败4.暂停 */
    private Integer status;

    /** 模型id */
    private Long modelId;

    /** 数据集id */
    private Long datasetId;

    /** 服务pv名称 */
    private String persistentVolumeName;

    /** 服务pvc名称 */
    private String persistentVolumeClaimName;
}
