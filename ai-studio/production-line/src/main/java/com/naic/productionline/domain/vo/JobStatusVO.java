package com.naic.productionline.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * job 状态表 job_status_vo
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="job状态表VO")
public class JobStatusVO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "job名称")
    private String jobName;

    @ApiModelProperty(value = "active")
    private Integer active;

    @ApiModelProperty(value = "Succeeded")
    private Integer succeeded;

    @ApiModelProperty(value = "Failed")
    private Integer failed;
}
