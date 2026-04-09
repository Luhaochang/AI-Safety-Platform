package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * jupyter表对象 jupyter_image
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("jupyter_image")
@ApiModel(value="jupyter表对象")
public class JupyterImage
{
    private static final long serialVersionUID = 1L;

    /** 自增id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** jupyter名称 */
    private String name;

    /** jupyter镜像文件url */
    private String fileUrl;

    /** 是否需要gpu资源，0.不需要1.需要 */
    private Integer gpuRequired;
}
