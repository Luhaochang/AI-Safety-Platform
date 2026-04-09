package com.naic.api.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author HuZhenSha
 * @since 2021/10/11
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("token payload")
public class Payload {

    @ApiModelProperty("主题")
    private String sub;

    @ApiModelProperty("签发时间")
    private Long iat;

    @ApiModelProperty("过期时间")
    private Long exp;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名称")
    private String username;

}
