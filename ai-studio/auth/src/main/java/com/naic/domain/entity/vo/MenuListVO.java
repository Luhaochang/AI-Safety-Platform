package com.naic.domain.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.naic.api.entity.vo.BaseEntityVO;
import com.naic.domain.TreeSelect;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 菜单显示信息
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="MenuListVO")
public class MenuListVO extends BaseEntityVO {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "checkedKeys")
    private List<Long> checkedKeys;

    @ApiModelProperty(value = "menus")
    private List<TreeSelect> menus;

}
