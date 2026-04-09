package com.naic.domain.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.naic.api.entity.po.BaseEntity;
import com.naic.api.entity.vo.BaseEntityVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 岗位表 sys_post
 * 
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@TableName("sys_post")
@ApiModel(value="岗位表对象")
public class SysPostPO extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 岗位序号 */
    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;

    /** 岗位编码 */
    private String postCode;

    /** 岗位名称 */
    private String postName;

    /** 岗位排序 */
    private Integer postSort;

    /** 状态（0正常 1停用） */
    private String status;

}
