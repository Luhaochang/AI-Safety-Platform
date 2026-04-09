package com.naic.productionline.domain.cnv;

import com.naic.productionline.domain.dto.JupyterServiceDTO;
import com.naic.productionline.domain.dto.ModelServiceDTO;
import com.naic.productionline.domain.po.JupyterServicePO;
import com.naic.productionline.domain.po.ModelServicePO;
import com.naic.productionline.domain.vo.JupyterServiceVO;
import com.naic.productionline.domain.vo.ModelServiceVO;
import org.mapstruct.Mapper;

/**
 *
 * @author wangyunong
 */
@Mapper(componentModel = "spring")
public interface JupyterServiceCnv {

    /**
     * dto to po
     * @param dto dto
     * @return po JupyterServicePO
     */
    public abstract JupyterServicePO dtoToPo(JupyterServiceDTO dto);

    /**
     * po to vo
     * @param po po
     * @return JupyterServiceVO
     */
    public abstract JupyterServiceVO poToVo(JupyterServicePO po);
}
