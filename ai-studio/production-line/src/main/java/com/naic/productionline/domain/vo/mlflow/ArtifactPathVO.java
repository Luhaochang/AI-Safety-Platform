package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 实验结果对象VO artifact_path_vo
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="实验结果对象VO")
public class ArtifactPathVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件路径")
    @JsonProperty("path")
    private String path;

    @ApiModelProperty(value = "文件实际路径")
    @JsonProperty("basePath")
    private String basePath;

    @ApiModelProperty(value = "是否文件夹")
    @JsonProperty("is_dir")
    private Boolean isDir;

    @ApiModelProperty(value = "文件大小")
    @JsonProperty("file_size")
    private Long fileSize;

}
