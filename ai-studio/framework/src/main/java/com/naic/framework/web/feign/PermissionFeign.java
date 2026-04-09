package com.naic.framework.web.feign;

import com.naic.framework.api.Result;
import com.naic.framework.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author wangyunong
 */
@FeignClient(name ="ai-studio-auth", configuration = FeignClientConfig.class)
public interface PermissionFeign {
    /**
     * 查询是否拥有权限
     * @param permission 权限字符
     * @return {@link Result <Boolean>}
     */
    @GetMapping("/permission/has-permission")
    Result<Boolean> hasPermission(@RequestParam("permission") String permission);

}
