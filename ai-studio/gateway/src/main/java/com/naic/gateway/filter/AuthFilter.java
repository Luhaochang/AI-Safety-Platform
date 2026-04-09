package com.naic.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.naic.gateway.feign.TokenService;
import com.naic.gateway.utils.MatchUtil;
import com.naic.api.api.DecodedTokenInfo;
import com.naic.api.api.Payload;
import com.naic.api.api.Result;
import com.naic.api.api.ResultCode;
import com.naic.api.constant.AuthorizationConstant;
import com.naic.api.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.naic.api.utils.SpringUtils.removePrefix;

/**
 * @author HuZhenSha
 * @since 2021/9/15
 */
@Component
@PropertySource("classpath:gateway.properties")
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    private Map<String, List<Long>> permMap = null;

    @Value("${gateway.url.ignored}")
    private List<String> globalIgnoredList;
    private final TokenService tokenService;

    public AuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;

    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String url = exchange.getRequest().getURI().getPath();
        String method = request.getMethodValue();
        HttpHeaders headers = request.getHeaders();

        if (SpringUtils.isEmpty(url)){
            return chain.filter(exchange);
        }

        // 1. 对应跨域的预检请求直接放行
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        //2.放行login操作
        AntPathMatcher path = new AntPathMatcher();
        if(path.match("/auth/login/**", url)){
            return chain.filter(exchange);
        }

        // 3. 校验token
        String accessToken = null;
        List<String> accessTokenHeader = request.getHeaders().get(AuthorizationConstant.ACCESS_TOKEN_KEYWORD);
        if (accessTokenHeader != null){
            accessToken = removePrefix(accessTokenHeader.get(0), "Bearer ");
        }

        if (SpringUtils.isEmpty(accessToken)){

            // 2. 如果在白名单中
            if (MatchUtil.isMatchInList(url, globalIgnoredList)){
                return chain.filter(exchange);
            }

            // 没有token，不予访问
            System.out.println("没有token，不予访问");
            return returnErrorResult(exchange, Result.error(ResultCode.UN_AUTHORIZED));
        }


        // 校验令牌
        DecodedTokenInfo decodedToken = getTokenData(accessToken);
        if (decodedToken == null || decodedToken.getPayload() == null){
            System.out.println("校验令牌，不予访问");
            return returnErrorResult(exchange, Result.error(ResultCode.UN_AUTHORIZED));
        }

        if (decodedToken.getNewToken() != null){
            // 写回 refreshed access token
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add(AuthorizationConstant.EXPOSE_HEADER, AuthorizationConstant.ACCESS_TOKEN_KEYWORD);
            if ( ! response.getHeaders().containsKey(AuthorizationConstant.ACCESS_TOKEN_KEYWORD)){
                response.getHeaders().add(AuthorizationConstant.ACCESS_TOKEN_KEYWORD, decodedToken.getNewToken());
            }
        }
        // 请求头加入当前用户的id，下沉到资源服务
        // 不能直接调用exchange.getRequest().getHeaders().add()方法 这里的headers为ReadOnlyHttpHeaders
        Payload payload = decodedToken.getPayload();
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
        requestBuilder.header(AuthorizationConstant.LOGIN_USER_ID_HEADER, String.valueOf(payload.getUserId()));
        requestBuilder.header(AuthorizationConstant.LOGIN_USER_NAME_HEADER, String.valueOf(payload.getUsername()));
        exchange.mutate().request(requestBuilder.build()).build();

        //打印请求
        log.info("--->method: {} URI: {} header: {}", method, url, headers);
        // 没找到匹配资源，放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }


    /**
     * 返回错误信息
     * @param exchange ServerWebExchange
     * @param result 需返回的Result
     * @return {@link Mono<Void>}
     */
    private Mono<Void> returnErrorResult(ServerWebExchange exchange, Result<?> result){
        //ServerHttpRequest ServerHttpResponse  Webflux不同于Web
        ServerHttpResponse response = exchange.getResponse();
        String res = JSON.toJSONString(result);
        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(res.getBytes());
        response.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
        return response.writeWith(Mono.just(bodyDataBuffer));
    }

    private DecodedTokenInfo getTokenData(String accessToken) {
        // 调用 校验令牌
        Map<String, String> map = new HashMap<>(1);
        map.put("token", accessToken);
        Result<DecodedTokenInfo> res = tokenService.checkToken(map);
        if (res.getCode() == HttpServletResponse.SC_OK){
            return res.getData();
        }
        return null;
    }


}
