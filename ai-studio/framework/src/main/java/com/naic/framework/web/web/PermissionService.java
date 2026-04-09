package com.naic.framework.web.web;

import com.naic.framework.web.feign.PermissionFeign;
import org.springframework.stereotype.Service;

/**
 * 自定义权限实现，ss取自SpringSecurity首字母
 * 
 * @author wangyunong
 */
@Service("isPermitted")
public class PermissionService
{
    private final PermissionFeign feign;

    public PermissionService(PermissionFeign feign) {
        this.feign = feign;
    }

    public boolean hasPermission(String permission)
    {
        boolean per=feign.hasPermission(permission).getData();
        return per;
    }
}
