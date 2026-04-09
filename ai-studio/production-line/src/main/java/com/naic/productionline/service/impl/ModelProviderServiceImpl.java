package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.productionline.domain.cnv.ModelProviderCnv;
import com.naic.productionline.domain.po.ModelProviderPO;
import com.naic.productionline.domain.vo.ModelProviderVO;
import com.naic.productionline.mapper.ModelProviderMapper;
import com.naic.productionline.service.ModelProviderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型提供方 服务实现
 *
 * @author xingdong
 */
@Service
@AllArgsConstructor
public class ModelProviderServiceImpl extends ServiceImpl<ModelProviderMapper, ModelProviderPO> implements ModelProviderService {

    private final ModelProviderMapper ModelProviderMapper;
    private final ModelProviderCnv ModelProviderCnv;

    @Override
    public List<ModelProviderVO> getAll() {
        List<ModelProviderVO> voList = new ArrayList<>();
        List<ModelProviderPO> poList = ModelProviderMapper.selectList(null);
        for (ModelProviderPO po : poList) {
            voList.add(ModelProviderCnv.poToVo(po));
        }
        return voList;
    }
}
