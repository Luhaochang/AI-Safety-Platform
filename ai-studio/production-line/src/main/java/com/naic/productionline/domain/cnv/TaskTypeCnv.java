package com.naic.productionline.domain.cnv;


import com.naic.productionline.domain.dto.TaskTypeDTO;
import com.naic.productionline.domain.po.TaskTypePO;
import com.naic.productionline.domain.vo.TaskTypeVO;
import org.mapstruct.Mapper;

/**
 *
 * @author xingdong
 */
@Mapper(componentModel = "spring")
public interface TaskTypeCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po ModelPO
     */
    public abstract TaskTypePO dtoToPo(TaskTypeDTO dto);

    /**
     * po to vo
     * @param po po
     * @return ModelVO
     */
    public abstract TaskTypeVO poToVo(TaskTypePO po);
}
