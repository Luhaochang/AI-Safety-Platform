package com.naic.domain.entity.vo;

import com.naic.api.entity.vo.BaseEntityVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 用户info vo对象 SysUserInfoVO
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="SysUserProfileVO")
public class SysUserProfileVO extends BaseEntityVO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "user")
    private SysUserVO user;

    @ApiModelProperty(value = "角色")
    private String roleGroup;

    @ApiModelProperty(value = "岗位")
    private String postGroup;
}
