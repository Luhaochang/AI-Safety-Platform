package com.naic.domain.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.naic.api.entity.po.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户和角色关联 sys_user_role
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("sys_user_role")
@ApiModel(value="用户和角色关联表对象")
public class SysUserRolePO extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(value = "user_id")
    private Long userId;
    
    /** 角色ID */
    private Long roleId;

}
