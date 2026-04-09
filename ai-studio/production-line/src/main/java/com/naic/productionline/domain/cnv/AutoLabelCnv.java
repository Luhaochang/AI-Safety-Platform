package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.AutoLabelDTO;
import com.naic.productionline.domain.po.AutoLabelPO;
import com.naic.productionline.domain.vo.AutoLabelVO;
import org.mapstruct.Mapper;

/**
 *
 * @author wangyunong
 */
@Mapper(componentModel = "spring")
public interface AutoLabelCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po AutoLabelPO
     */
    public abstract AutoLabelPO dtoToPo(AutoLabelDTO dto);

    /**
     * po to vo
     * @param po po
     * @return AutoLabelVO
     */
    public abstract AutoLabelVO poToVo(AutoLabelPO po);
}
