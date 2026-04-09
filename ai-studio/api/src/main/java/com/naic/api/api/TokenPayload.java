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
@ApiModel("token载荷")
public class TokenPayload extends Payload{

    @ApiModelProperty("认证中心ticket")
    private String ticket;

    public TokenPayload(String sub, Long iat, Long exp, Long userId, String username, String ticket){
        super(sub, iat, exp, userId, username);
        this.ticket = ticket;
    }

    public TokenPayload(Payload payload, String ticket){
        super(payload.getSub(), payload.getIat(), payload.getExp(), payload.getUserId(), payload.getUsername());
        this.ticket = ticket;
    }

}
