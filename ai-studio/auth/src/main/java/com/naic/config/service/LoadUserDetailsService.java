package com.naic.config.service;


import com.naic.config.entity.LoginUserDetails;

import java.util.Map;

/**
 * @author HuZhenSha
 * @since 2021/10/13
 */
public interface LoadUserDetailsService {

    /**
     * 用户登录
     * @param map map
     * @return {@link LoginUserDetails} user ticket
     */
    LoginUserDetails loadByUsername(Map<String, String> map);

    /**
     * 用户登录
     * @param map map
     * @return {@link LoginUserDetails} user ticket
     */
    LoginUserDetails loadByTel(Map<String, String> map);

}
