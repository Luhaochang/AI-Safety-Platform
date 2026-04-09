package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.ModelTagDTO;
import com.naic.productionline.domain.po.ModelTagPO;
import com.naic.productionline.domain.vo.ModelTagVO;
import org.mapstruct.Mapper;

/**
 *
 * @author wangyunong
 */
@Mapper(componentModel = "spring")
public interface ModelTagCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po ModelTagPO
     */
    public abstract ModelTagPO dtoToPo(ModelTagDTO dto);

    /**
     * po to vo
     * @param po po
     * @return ModelTagVO
     */
    public abstract ModelTagVO poToVo(ModelTagPO po);
}
