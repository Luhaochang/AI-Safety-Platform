package com.naic.api.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author HuZhenSha
 * @since 2021/9/14
 */
public class ResponseJsonData {

    public static void response(HttpServletResponse response, Object src){
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter pw;
        try {
            pw = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // SerializerFeature.WriteMapNullValue,
        //                SerializerFeature.WriteNullStringAsEmpty,
        //                SerializerFeature.WriteNullNumberAsZero
        SerializerFeature[] feature= {SerializerFeature.WriteMapNullValue};

        pw.write(JSON.toJSONString(src, feature));
        pw.flush();
        pw.close();
    }

}
