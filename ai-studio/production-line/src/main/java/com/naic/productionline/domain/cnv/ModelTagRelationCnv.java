package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.ModelTagRelationDTO;
import com.naic.productionline.domain.po.ModelTagRelationPO;
import com.naic.productionline.domain.vo.ModelTagRelationVO;
import org.mapstruct.Mapper;

/**
 *
 * @author wangyunong
 */
@Mapper(componentModel = "spring")
public interface ModelTagRelationCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po ModelTagRelationPO
     */
    public abstract ModelTagRelationPO dtoToPo(ModelTagRelationDTO dto);

    /**
     * po to vo
     * @param po po
     * @return ModelTagRelationVO
     */
    public abstract ModelTagRelationVO poToVo(ModelTagRelationPO po);
}
