package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.productionline.domain.cnv.AppSceneCnv;
import com.naic.productionline.domain.po.AppScenePO;
import com.naic.productionline.domain.vo.AppSceneVO;
import com.naic.productionline.mapper.AppSceneMapper;
import com.naic.productionline.service.AppSceneService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用场景 服务实现
 *
 * @author xingdong
 */
@Service
@AllArgsConstructor
public class AppSceneServiceImpl extends ServiceImpl<AppSceneMapper, AppScenePO> implements AppSceneService {

    private final AppSceneMapper AppSceneMapper;
    private final AppSceneCnv AppSceneCnv;

    @Override
    public List<AppSceneVO> getAll() {
        List<AppSceneVO> voList = new ArrayList<>();
        List<AppScenePO> poList = AppSceneMapper.selectList(null);
        for (AppScenePO po : poList) {
            voList.add(AppSceneCnv.poToVo(po));
        }
        return voList;
    }
}
