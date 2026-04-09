package com.naic.domain.entity.cnv;

import com.naic.api.entity.vo.RegisterUserVO;
import com.naic.domain.entity.dto.SysUserDTO;
import com.naic.domain.entity.po.SysUserPO;
import com.naic.domain.entity.vo.SysUserVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SysUserCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po SysUserPO
     */
    public abstract SysUserPO dtoToPo(SysUserDTO dto);

    /**
     * po to vo
     * @param po po
     * @return SysUserVO
     */
    public abstract SysUserVO poToVo(SysUserPO po);

    /**
     * po to RegisterUser
     * @param po po
     * @return {@link RegisterUserVO}
     */
    public abstract RegisterUserVO poToRegisterUser(SysUserPO po);
}
