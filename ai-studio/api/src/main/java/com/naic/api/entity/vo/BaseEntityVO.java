package com.naic.api.entity.vo;

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
 * @author zhu
 */
public class BaseEntityVO implements Serializable {
    @ApiModelProperty("创建人")
    private Long createUser;

    @ApiModelProperty(value = "创建者姓名")
    private String createUserName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    private Long updateUser;

    @ApiModelProperty(value = "创建者姓名")
    private String updateUserName;

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


    @ApiModelProperty("请求参数")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> params;

    public BaseEntityVO() {
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

    public Map<String, Object> getParams()
    {
        if (params == null)
        {
            params = new HashMap<>();
        }
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }

    public Long getCreateUser() {
        return this.createUser;
    }

    public String getCreateUserName(){return this.createUserName;}

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public Long getUpdateUser() {
        return this.updateUser;
    }

    public String getUpdateUserName(){return this.updateUserName;}

    public LocalDateTime getUpdateTime() {
        return this.updateTime;
    }

    public void setCreateUser(final Long createUser) {
        this.createUser = createUser;
    }

    public void setCreateUserName(final String createUserName) {
        this.createUserName = createUserName;
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

    public void setUpdateUserName(final String updateUserName) {
        this.updateUserName = updateUserName;
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
        } else if (!(o instanceof BaseEntityVO)) {
            return false;
        } else {
            BaseEntityVO other = (BaseEntityVO)o;
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

                Object thisCreateUserName = this.getCreateUserName();
                Object otherCreateUserName = other.getCreateUserName();
                if (thisCreateUserName == null) {
                    if (otherCreateUserName != null) {
                        return false;
                    }
                } else if (!thisCreateUserName.equals(otherCreateUserName)) {
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

                Object thisUpdateUserName = this.getUpdateUserName();
                Object otherUpdateUserName = other.getUpdateUserName();
                if (thisUpdateUserName == null) {
                    if (otherUpdateUserName != null) {
                        return false;
                    }
                } else if (!thisUpdateUserName.equals(otherUpdateUserName)) {
                    return false;
                }

                label55: {
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

                Object thisUpdateTime = this.getUpdateTime();
                Object otherUpdateTime = other.getUpdateTime();
                if (thisUpdateTime == null) {
                    return otherUpdateTime == null;
                } else {
                    return thisUpdateTime.equals(otherUpdateTime);
                }
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BaseEntityVO;
    }

    @Override
    public int hashCode() {
        int result = 1;
        Object createUser = this.getCreateUser();
        result = result * 59 + (createUser == null ? 43 : createUser.hashCode());
        Object createUserName = this.getCreateUserName();
        result = result * 59 + (createUserName == null ? 43 : createUserName.hashCode());
        Object updateUser = this.getUpdateUser();
        result = result * 59 + (updateUser == null ? 43 : updateUser.hashCode());
        Object updateUserName = this.getUpdateUserName();
        result = result * 59 + (updateUserName == null ? 43 : updateUserName.hashCode());
        Object createTime = this.getCreateTime();
        result = result * 59 + (createTime == null ? 43 : createTime.hashCode());
        Object updateTime = this.getUpdateTime();
        result = result * 59 + (updateTime == null ? 43 : updateTime.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "BaseEntity(createUser=" + this.getCreateUser() +", createUserName=" + this.getCreateUserName()  + ", createTime=" + this.getCreateTime() + ", updateUser=" + this.getUpdateUser() +", updateUserName=" + this.getUpdateUserName() + ", updateTime=" + this.getUpdateTime() + ")";
    }
}
