package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.ModelDTO;
import com.naic.productionline.domain.po.ModelPO;
import com.naic.productionline.domain.vo.ModelVO;
import org.mapstruct.Mapper;

/**
 *
 * @author wangyunong
 */
@Mapper(componentModel = "spring")
public interface ModelCnv {

    /**
     * dto to po
     *
     * @param dto dto
     * @return po ModelPO
     */
    public abstract ModelPO dtoToPo(ModelDTO dto);

    /**
     * po to vo
     *
     * @param po po
     * @return ModelVO
     */
    public abstract ModelVO poToVo(ModelPO po);
}