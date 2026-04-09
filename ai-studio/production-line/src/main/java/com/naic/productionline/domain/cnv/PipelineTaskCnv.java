package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.PipelineTaskDTO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.PipelineTaskVO;
import org.mapstruct.Mapper;

/**
 *
 * @author wangyunong
 */
@Mapper(componentModel = "spring")
public interface PipelineTaskCnv {

    /**
     * dto to po
     *
     * @param dto dto
     * @return po PipelineTaskPO
     */
    public abstract PipelineTaskPO dtoToPo(PipelineTaskDTO dto);

    /**
     * po to vo
     *
     * @param po po
     * @return PipelineTaskVO
     */
    public abstract PipelineTaskVO poToVo(PipelineTaskPO po);
}