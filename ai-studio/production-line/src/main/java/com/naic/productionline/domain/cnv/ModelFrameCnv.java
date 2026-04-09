package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.ModelFrameDTO;
import com.naic.productionline.domain.po.ModelFramePO;
import com.naic.productionline.domain.vo.ModelFrameVO;
import org.mapstruct.Mapper;

/**
 *
 * @author xingdong
 */
@Mapper(componentModel = "spring")
public interface ModelFrameCnv {
    /**
     * dto to po
     *
     * @param dto dto
     * @return po ModelFramePO
     */
    public abstract ModelFramePO dtoToPo(ModelFrameDTO dto);

    /**
     * po to vo
     *
     * @param po po
     * @return ModelFrameVO
     */
    public abstract ModelFrameVO poToVo(ModelFramePO po);
}
