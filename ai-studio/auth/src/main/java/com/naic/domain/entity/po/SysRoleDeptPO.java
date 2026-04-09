package com.naic.domain.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.naic.api.entity.po.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 角色和部门关联 sys_role_dept
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("sys_role")
@ApiModel(value="角色和部门关联表对象")
public class SysRoleDeptPO extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @TableId(value = "role_id")
    private Long roleId;
    
    /** 部门ID */

    private Long deptId;

}
