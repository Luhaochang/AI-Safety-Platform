package com.naic.api.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HuZhenSha
 * @date 2021/9/10 14:35
 */
public class BaseEntity implements Serializable {

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @ApiModelProperty("搜索值")
    private String searchValue;

    @ApiModelProperty("备注")
    private String remark;


    public BaseEntity() {
    }

    public String getSearchValue()
    {
        return searchValue;
    }

    public void setSearchValue(String searchValue)
    {
        this.searchValue = searchValue;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }


    public Long getCreateUser() {
        return this.createUser;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public Long getUpdateUser() {
        return this.updateUser;
    }

    public LocalDateTime getUpdateTime() {
        return this.updateTime;
    }

    public void setCreateUser(final Long createUser) {
        this.createUser = createUser;
    }

    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    public void setCreateTime(final LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateUser(final Long updateUser) {
        this.updateUser = updateUser;
    }

    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    public void setUpdateTime(final LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof BaseEntity)) {
            return false;
        } else {
            BaseEntity other = (BaseEntity) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object thisCreateUser = this.getCreateUser();
                Object otherCreateUser = other.getCreateUser();
                if (thisCreateUser == null) {
                    if (otherCreateUser != null) {
                        return false;
                    }
                } else if (!thisCreateUser.equals(otherCreateUser)) {
                    return false;
                }

                Object thisUpdateUser = this.getUpdateUser();
                Object otherUpdateUser = other.getUpdateUser();
                if (thisUpdateUser == null) {
                    if (otherUpdateUser != null) {
                        return false;
                    }
                } else if (!thisUpdateUser.equals(otherUpdateUser)) {
                    return false;
                }

                label55:
                {
                    Object thisCreateTime = this.getCreateTime();
                    Object otherCreateTime = other.getCreateTime();
                    if (thisCreateTime == null) {
                        if (otherCreateTime == null) {
                            break label55;
                        }
                    } else if (thisCreateTime.equals(otherCreateTime)) {
                        break label55;
                    }

                    return false;
                }

                Object this$updateTime = this.getUpdateTime();
                Object other$updateTime = other.getUpdateTime();
                if (this$updateTime == null) {
                    return other$updateTime == null;
                } else {
                    return this$updateTime.equals(other$updateTime);
                }
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BaseEntity;
    }

    @Override
    public int hashCode() {
        int result = 1;
        Object createUser = this.getCreateUser();
        result = result * 59 + (createUser == null ? 43 : createUser.hashCode());
        Object updateUser = this.getUpdateUser();
        result = result * 59 + (updateUser == null ? 43 : updateUser.hashCode());
        Object createTime = this.getCreateTime();
        result = result * 59 + (createTime == null ? 43 : createTime.hashCode());
        Object updateTime = this.getUpdateTime();
        result = result * 59 + (updateTime == null ? 43 : updateTime.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "BaseEntity(createUser=" + this.getCreateUser() + ", createTime=" + this.getCreateTime() + ", updateUser=" + this.getUpdateUser() + ", updateTime=" + this.getUpdateTime() + ")";
    }
}