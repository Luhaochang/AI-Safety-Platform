package com.naic.productionline.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.naic.api.entity.vo.BaseEntityVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 模型对象 model_vo
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value = "模型对象VO")
public class ModelVOOld extends BaseEntityVO {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "自增id")
    private Long id;

    @ApiModelProperty(value = "模型名称")
    private String name;

    @ApiModelProperty(value = "模型场景")
    private Integer scene;

    @ApiModelProperty(value = "模型描述")
    private String description;

    @ApiModelProperty(value = "模型文件url")
    private String fileUrl;

    @ApiModelProperty(value = "模型参数")
    private String paramsString;

    @ApiModelProperty(value = "模型作者")
    private String author;

    @ApiModelProperty(value = "是否官方数据集：0.否 1.是")
    private Integer isOfficial;

    @ApiModelProperty(value = "configMap内容")
    private String configMap;

    @ApiModelProperty(value = "标签信息")
    private List<ModelTagVO> tagList;

    @ApiModelProperty(value = "readme")
    private String readme;

    @ApiModelProperty(value = "二级应用场景id")
    private Long secondaryScene;

    @ApiModelProperty(value = "二级应用场景name")
    private String secondarySceneName;

    @ApiModelProperty(value = "模型输入输出说明")
    private String modelPut;

    @ApiModelProperty(value = "模型结果类别字典")
    private String classDict;

}
