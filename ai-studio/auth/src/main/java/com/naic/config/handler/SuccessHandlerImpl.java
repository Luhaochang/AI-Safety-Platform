package com.naic.config.handler;

import com.alibaba.fastjson.JSON;
import com.naic.api.api.Payload;
import com.naic.api.api.Result;
import com.naic.api.api.TokenPayload;
import com.naic.api.constant.AuthorizationConstant;
import com.naic.api.entity.vo.RegisterUserVO;
import com.naic.api.utils.ResponseJsonData;
import com.naic.config.entity.LoginUserDetails;
import com.naic.service.auth.LoginUserDaoService;
import com.naic.utils.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.naic.api.constant.AuthorizationConstant.EXPOSE_HEADER;

/**
 * 登录成功 登出成功Handler
 * @author HuZhenSha
 * @since 2021/9/13
 */
@Component
@AllArgsConstructor
public class SuccessHandlerImpl implements AuthenticationSuccessHandler, LogoutSuccessHandler {

    private final LoginUserDaoService loginUserDaoService;

//    public SuccessHandlerImpl(LoginUserDaoService loginUserDaoService) {
//        this.loginUserDaoService = loginUserDaoService;
//    }

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 取出登录用户
        LoginUserDetails details = (LoginUserDetails) authentication.getPrincipal();
        RegisterUserVO user = details.getUser();

        // 退出之前的登录用户
        String preToken = request.getHeader(AuthorizationConstant.ACCESS_TOKEN_KEYWORD);
        loginUserDaoService.removeUserByToken(preToken);

        // 生成ticket, 可用来刷新token
        long mills = System.currentTimeMillis();
        String ticket = JwtUtil.generateTokenByHmac(JSON.toJSONString(new Payload(AuthorizationConstant.JWT_SUBJECT, mills,
                mills + AuthorizationConstant.TICKET_EXPIRE_TIME, user.getUserId(), user.getUserName())));
        loginUserDaoService.addUser(ticket, user);
        // 生成token
        TokenPayload tokenPayload = new TokenPayload(AuthorizationConstant.JWT_SUBJECT, mills, mills + AuthorizationConstant.ACCESS_TOKEN_EXPIRE_TIME,
                user.getUserId(), user.getUserName(), ticket);
        String accessToken = JwtUtil.generateTokenByHmac(JSON.toJSONString(tokenPayload));
        // 回写
        response.setHeader(EXPOSE_HEADER, AuthorizationConstant.ACCESS_TOKEN_KEYWORD + "," + AuthorizationConstant.TICKET_KEYWORD);
        response.setHeader(AuthorizationConstant.ACCESS_TOKEN_KEYWORD, accessToken);
        response.setHeader(AuthorizationConstant.TICKET_KEYWORD, ticket);

        ResponseJsonData.response(response, Result.success(user));
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        ResponseJsonData.response(response, Result.success());
    }
}
