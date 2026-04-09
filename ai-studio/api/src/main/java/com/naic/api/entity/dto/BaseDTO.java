package com.naic.api.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.naic.api.entity.po.BaseEntity;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HuZhenSha
 * @since 2021/9/17
 */
public class BaseDTO implements Serializable {

    @ApiModelProperty("创建人")
    private Long createUser;

    @ApiModelProperty("更新人")
    private Long updateUser;


    @JsonIgnore
    @ApiModelProperty("搜索值")
    private String searchValue;


    @ApiModelProperty("备注")
    private String remark;


    @ApiModelProperty("请求参数")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> params;

    public BaseDTO() {
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

    public Long getUpdateUser() {
        return this.updateUser;
    }

    public void setCreateUser(final Long createUser) {
        this.createUser = createUser;
    }

    public void setUpdateUser(final Long updateUser) {
        this.updateUser = updateUser;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof BaseDTO)) {
            return false;
        } else {
            BaseDTO other = (BaseDTO)o;
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

            }
        }
        return false;
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
        return result;
    }

    @Override
    public String toString() {
        return "BaseEntity(createUser=" + this.getCreateUser() + ", updateUser=" + this.getUpdateUser() + ")";
    }

}
