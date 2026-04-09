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
@ApiModel(value="SysUserInfoVO")
public class SysUserInfoVO extends BaseEntityVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "roles")
    private List<SysRoleVO> roles;

    @ApiModelProperty(value = "posts")
    private List<SysPostVO> posts;

    @ApiModelProperty(value = "data")
    private SysUserVO data;

    @ApiModelProperty(value = "postIds")
    private List<Long> postIds;

    @ApiModelProperty(value = "roleIds")
    private List<Long> roleIds;

}
