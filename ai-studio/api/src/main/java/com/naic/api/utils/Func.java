package com.naic.api.utils;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author HuZhenSha
 * @since 2021/9/13
 */
public class Func {

    public static List<Long> toLongList(String str){
        return Arrays.asList(toLongArray(str));
    }

    public static Long[] toLongArray(String str) {
        if (StringUtils.isEmpty(str)) {
            return new Long[0];
        } else {
            String[] arr = str.split(",");
            Long[] longs = new Long[arr.length];
            for(int i = 0; i < arr.length; ++i) {
                Long v = Long.getLong(arr[i]);
                longs[i] = v;
            }
            return longs;
        }
    }

    public static boolean isEmpty(Object obj){
        return ObjectUtils.isEmpty(obj);
    }

    /**
     * 对象组中是否存在 Empty Object
     * @param os 对象组, 可传数组或者多个参数
     * @return boolean
     */
    public static boolean hasEmpty(Object... os) {
        for (Object o : os) {
            if (isEmpty(o)) {
                return true;
            }
        }
        return false;
    }

}
