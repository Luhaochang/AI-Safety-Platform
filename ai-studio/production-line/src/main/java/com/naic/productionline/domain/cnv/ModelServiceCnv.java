package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.ModelServiceDTO;
import com.naic.productionline.domain.po.ModelServicePO;
import com.naic.productionline.domain.vo.ModelServiceVO;
import org.mapstruct.Mapper;

/**
 *
 * @author wangyunong
 */
@Mapper(componentModel = "spring")
public interface ModelServiceCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po ModelServicePO
     */
    public abstract ModelServicePO dtoToPo(ModelServiceDTO dto);

    /**
     * po to vo
     * @param po po
     * @return ModelServiceVO
     */
    public abstract ModelServiceVO poToVo(ModelServicePO po);
}
