package com.naic.gateway.utils;

import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * @author HuZhenSha
 * @since 2021/9/15
 */
public class MatchUtil {

    /**
     * 判断url是否匹配
     * @param url url
     * @param list 模式集合
     * @return boolean
     */
    public static boolean isMatchInList(String url, List<String> list){
        AntPathMatcher path = new AntPathMatcher();
        for (String pattern : list) {
            if (path.match(pattern, url)){
                return true;
            }
        }
        return false;
    }

}
