package com.naic.api.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author HuZhenSha
 * @since 2021/11/11
 */
@Slf4j
public class LogUtil {

    public static void logInfo(String description, LocalDateTime time, String method, Object[] parameter, Object response){
        log.info(JSON.toJSONString(new InfoLog(description, time, method, Arrays.toString(parameter), String.valueOf(response))));
    }

    public static void logError(String description, LocalDateTime time, String method, Object[] parameter, String error){
        log.error(JSON.toJSONString(new ErrorLog(description, time, method, Arrays.toString(parameter), error)));
    }

}
