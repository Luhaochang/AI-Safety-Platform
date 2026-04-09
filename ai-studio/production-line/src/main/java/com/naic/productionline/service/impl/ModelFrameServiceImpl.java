package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.productionline.domain.cnv.ModelFrameCnv;
import com.naic.productionline.domain.po.ModelFramePO;
import com.naic.productionline.domain.vo.ModelFrameVO;
import com.naic.productionline.mapper.ModelFrameMapper;
import com.naic.productionline.service.ModelFrameService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型框架 服务实现
 *
 * @author xingdong
 */
@Service
@AllArgsConstructor
public class ModelFrameServiceImpl extends ServiceImpl<ModelFrameMapper, ModelFramePO> implements ModelFrameService {
    private final ModelFrameMapper ModelFrameMapper;
    private final ModelFrameCnv ModelFrameCnv;

    @Override
    public List<ModelFrameVO> getAll() {
        List<ModelFrameVO> voList = new ArrayList<>();
        List<ModelFramePO> poList = ModelFrameMapper.selectList(null);
        for (ModelFramePO po : poList) {
            voList.add(ModelFrameCnv.poToVo(po));
        }
        return voList;
    }
}
