package com.naic.domain.entity.cnv;

import com.naic.domain.entity.dto.SysPostDTO;
import com.naic.domain.entity.dto.SysUserDTO;
import com.naic.domain.entity.po.SysPostPO;
import com.naic.domain.entity.po.SysUserPO;
import com.naic.domain.entity.vo.SysPostVO;
import com.naic.domain.entity.vo.SysUserVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SysPostCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po SysPostPO
     */
    public abstract SysPostPO dtoToPo(SysPostDTO dto);

    /**
     * po to vo
     * @param po po
     * @return SysPostVO
     */
    public abstract SysPostVO poToVo(SysPostPO po);
}
