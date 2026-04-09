package com.naic.domain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.naic.api.entity.vo.BaseEntityVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 用户对象 UserInfoVO
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="UserInfoVO")
public class UserInfoVO extends BaseEntityVO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "user")
    private SysUserVO user;

    @ApiModelProperty(value = "roles")
    private Set<String> roles;

    @ApiModelProperty(value = "permissions")
    private Set<String> permissions;
}
