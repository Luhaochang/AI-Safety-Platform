package com.naic.domain.entity.cnv;

import com.naic.domain.entity.dto.SysRoleDTO;
import com.naic.domain.entity.po.SysRolePO;
import com.naic.domain.entity.vo.SysRoleVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SysRoleCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po UserInfo
     */
    public abstract SysRolePO dtoToPo(SysRoleDTO dto);

    /**
     * po to vo
     * @param po po
     * @return SysRoleVO
     */
    public abstract SysRoleVO poToVo(SysRolePO po);
}
