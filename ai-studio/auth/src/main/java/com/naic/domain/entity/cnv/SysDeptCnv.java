package com.naic.domain.entity.cnv;

import com.naic.domain.entity.dto.SysDeptDTO;
import com.naic.domain.entity.po.SysDeptPO;
import com.naic.domain.entity.vo.SysDeptVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SysDeptCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po SysDeptPO
     */
    public abstract SysDeptPO dtoToPo(SysDeptDTO dto);

    /**
     * po to vo
     * @param po po
     * @return SysDeptVO
     */
    public abstract SysDeptVO poToVo(SysDeptPO po);
}
