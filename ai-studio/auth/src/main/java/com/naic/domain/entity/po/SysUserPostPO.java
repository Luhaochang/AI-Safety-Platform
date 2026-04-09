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
 * 用户和岗位关联 sys_user_post
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("sys_user_post")
@ApiModel(value="用户和岗位关联表对象")
public class SysUserPostPO extends BaseEntity
{

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(value = "user_id")
    private Long userId;
    
    /** 岗位ID */
    private Long postId;

}
