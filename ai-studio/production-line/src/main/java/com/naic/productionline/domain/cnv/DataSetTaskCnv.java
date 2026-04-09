package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.DataSetTaskDTO;
import com.naic.productionline.domain.po.DataSetTaskPO;
import com.naic.productionline.domain.vo.DataSetTaskVO;
import org.mapstruct.Mapper;
/**
 *
 * @author wangyunong
 */
@Mapper(componentModel = "spring")
public interface DataSetTaskCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po DataSetTaskPO
     */
    public abstract DataSetTaskPO dtoToPo(DataSetTaskDTO dto);

    /**
     * po to vo
     * @param po po
     * @return DataSetTaskVO
     */
    public abstract DataSetTaskVO poToVo(DataSetTaskPO po);
}
