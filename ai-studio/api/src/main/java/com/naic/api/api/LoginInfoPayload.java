package com.naic.api.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * jwt payload
 * @author HuZhenSha
 * @since 2021/9/23
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ApiModel("token")
public class LoginInfoPayload extends Payload{

    @ApiModelProperty("角色id")
    private Long roleId;

    @ApiModelProperty("认证中心ticket")
    private String ticket;

    public LoginInfoPayload(String sub, Long iat, Long exp, Long userId, String username, Long roleId, String ticket){
        super(sub, iat, exp, userId, username);
        this.roleId = roleId;
        this.ticket = ticket;
    }

}
