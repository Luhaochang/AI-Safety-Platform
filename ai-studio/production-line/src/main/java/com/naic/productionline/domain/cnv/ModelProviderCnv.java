package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.ModelProviderDTO;
import com.naic.productionline.domain.po.ModelProviderPO;
import com.naic.productionline.domain.vo.ModelProviderVO;
import org.mapstruct.Mapper;

/**
 *
 * @author xingdong
 */
@Mapper(componentModel = "spring")
public interface ModelProviderCnv {
    /**
     * dto to po
     *
     * @param dto dto
     * @return po ModelProviderPO
     */
    public abstract ModelProviderPO dtoToPo(ModelProviderDTO dto);

    /**
     * po to vo
     *
     * @param po po
     * @return ModelProviderVO
     */
    public abstract ModelProviderVO poToVo(ModelProviderPO po);
}
