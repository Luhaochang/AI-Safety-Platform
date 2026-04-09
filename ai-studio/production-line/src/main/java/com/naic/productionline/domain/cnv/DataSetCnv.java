package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.DataSetDTO;
import com.naic.productionline.domain.po.DataSetPO;
import com.naic.productionline.domain.vo.DataSetVO;
import org.mapstruct.Mapper;

/**
 *
 * @author wangyunong
 */
@Mapper(componentModel = "spring")
public interface DataSetCnv {

    /**
     * dto to po
     *
     * @param dto dto
     * @return po DataSetPO
     */
    public abstract DataSetPO dtoToPo(DataSetDTO dto);

    /**
     * po to vo
     *
     * @param po po
     * @return DataSetVO
     */
    public abstract DataSetVO poToVo(DataSetPO po);
}