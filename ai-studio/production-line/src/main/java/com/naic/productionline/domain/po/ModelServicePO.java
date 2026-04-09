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
 * 模型对象 model_service
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("model_service")
@ApiModel(value="服务对象")
public class ModelServicePO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 服务名称 */
    private String name;

    /** 服务描述 */
    private String description;

    /** 模型id */
    private Long modelId;

    /** 服务url */
    private String serviceUrl;

    /** 服务运行名称 */
    private String serviceName;

    /** 部署状态：1.部署中2.部署完成3.部署失败4.暂停 */
    private Integer status;

    /** readme文件 */
    private String readme;

}
