package com.naic.productionline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.productionline.domain.cnv.TaskTypeCnv;
import com.naic.productionline.domain.po.TaskTypePO;
import com.naic.productionline.domain.vo.TaskTypeVO;
import com.naic.productionline.mapper.TaskTypeMapper;
import com.naic.productionline.service.TaskTypeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务类型 服务实现
 *
 * @author xingdong
 */
@Service
@AllArgsConstructor
public class TaskTypeServiceImpl extends ServiceImpl<TaskTypeMapper, TaskTypePO> implements TaskTypeService {

    private final TaskTypeMapper taskTypeMapper;
    private final TaskTypeCnv taskTypeCnv;

    @Override
    public List<TaskTypeVO> getAll() {
        List<TaskTypeVO> voList = new ArrayList<>();
        List<TaskTypePO> poList = taskTypeMapper.selectList(null);
        for (TaskTypePO po : poList) {
            voList.add(taskTypeCnv.poToVo(po));
        }
        return voList;
    }
}
