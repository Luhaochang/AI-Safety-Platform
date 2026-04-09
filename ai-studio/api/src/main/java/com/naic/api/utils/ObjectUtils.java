package com.naic.api.utils;

import org.springframework.lang.Nullable;

/**
 * @author HuZhenSha
 * @since 2021/9/16
 */
public class ObjectUtils extends org.springframework.util.ObjectUtils {

    /**
     * 判断元素不为空
     * @param obj object
     * @return boolean
     */
    public static boolean isNotEmpty(@Nullable Object obj) {
        return !ObjectUtils.isEmpty(obj);
    }

}