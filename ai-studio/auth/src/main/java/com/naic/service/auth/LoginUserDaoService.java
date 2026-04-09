package com.naic.service.auth;

import com.naic.api.entity.vo.RegisterUserVO;

/**
 * @author HuZhenSha
 * @since 2021/11/12
 */
public interface LoginUserDaoService {

    /**
     * 增加一个登录用户
     * @param ticket ticket
     * @param user user entity
     */
    void addUser(String ticket, RegisterUserVO user);

    /**
     * 用户下线
     * @param userId user id
     */
    void removeUserById(Long userId);

    /**
     * 用户下线
     * @param token token
     */
    void removeUserByToken(String token);

    /**
     * 用户下线
     * @param ticket ticket
     */
    void removeUserByTicket(String ticket);

    /**
     * 通过ticket获取当前登录用户信息
     * @param ticket ticket
     * @return user
     */
    RegisterUserVO loadUserByTicket(String ticket);

    /**
     * 检查登录
     * @param ticket ticket
     * @return bool
     */
    boolean checkLogin(String ticket);

}
