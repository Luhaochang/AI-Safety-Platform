package com.naic.domain.entity.cnv;

import com.naic.domain.entity.dto.SysDeptDTO;
import com.naic.domain.entity.dto.SysMenuDTO;
import com.naic.domain.entity.po.SysDeptPO;
import com.naic.domain.entity.po.SysMenuPO;
import com.naic.domain.entity.vo.SysDeptVO;
import com.naic.domain.entity.vo.SysMenuVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SysMenuCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po SysMenuPO
     */
    public abstract SysMenuPO dtoToPo(SysMenuDTO dto);

    /**
     * po to vo
     * @param po po
     * @return SysMenuVO
     */
    public abstract SysMenuVO poToVo(SysMenuPO po);
}
