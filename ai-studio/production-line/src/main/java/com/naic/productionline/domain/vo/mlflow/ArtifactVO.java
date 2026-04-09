package com.naic.productionline.domain.vo.mlflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * artifact对象VO artifact_vo
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="artifact对象VO")
public class ArtifactVO {

    private static final long serialVersionUID = 1L;

    @JsonProperty("root_uri")
    private String rootUri;

    @JsonProperty("files")
    private List<ArtifactPathVO> files;
}
