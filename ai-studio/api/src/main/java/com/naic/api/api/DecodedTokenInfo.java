package com.naic.api.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author HuZhenSha
 * @since 2021/10/11
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel("payload with token if token was expired")
public class DecodedTokenInfo {

    @ApiModelProperty("payload")
    public TokenPayload payload;

    @ApiModelProperty("new token (null if token is normal)")
    public String newToken;

}
