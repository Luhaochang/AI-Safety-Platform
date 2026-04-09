package com.naic.api.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 注册用户VO
 * @author wangyunong
 * @since 2024/9/2
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="UserVO对象")
public class RegisterUserVO {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "user_id")
    private Long userId;

    @ApiModelProperty(value = "用户账号")
    private String userName;

    @ApiModelProperty(value = "用户绑定手机号")
    private String phoneNumber;

    @ApiModelProperty(value = "用户名")
    private String nickName;

}
