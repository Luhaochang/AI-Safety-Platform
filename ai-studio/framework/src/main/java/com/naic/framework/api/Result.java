package com.naic.framework.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * des:
 *
 * @author HuZhenSha
 * @date 2021/3/5 15:35
 */

@ApiModel("Result")
@Data
public class Result<T> {

    @ApiModelProperty("结果代码，0为成功，负数为失败")
    private int code;
    @ApiModelProperty("提示信息")
    private String message;
    @ApiModelProperty("数据")
    private T data;


    private Result() {
    }

    public static<T> Result<T> success(){
        Result<T> result= new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        return result;
    }

    public static<T> Result<T> success(T t){
        Result<T> result= new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        result.data = t;
        return result;
    }

    public static<T> Result<T> error(IResultCode resultEnum){
        Result<T> result= new Result<>();
        result.code = resultEnum.getCode();
        result.message = resultEnum.getMessage();
        result.data = null;
        return result;
    }

    public static<T> Result<T> error(String msg){
        Result<T> result= new Result<>();
        result.code = ResultCode.FAILURE.getCode();
        result.message = msg;
        result.data = null;
        return result;
    }

}
