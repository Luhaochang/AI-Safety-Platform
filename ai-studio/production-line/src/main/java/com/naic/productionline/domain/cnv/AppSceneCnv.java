package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.AppSceneDTO;
import com.naic.productionline.domain.po.AppScenePO;
import com.naic.productionline.domain.vo.AppSceneVO;
import org.mapstruct.Mapper;

/**
 *
 * @author xingdong
 */
@Mapper(componentModel = "spring")
public interface AppSceneCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po ModelPO
     */
    public abstract AppScenePO dtoToPo(AppSceneDTO dto);

    /**
     * po to vo
     * @param po po
     * @return ModelVO
     */
    public abstract AppSceneVO poToVo(AppScenePO po);
}
