package com.naic.productionline.service;

import com.naic.productionline.domain.vo.TaskTypeVO;

import java.util.List;

/**
 * 任务类型 业务层
 *
 * @author xingdong
 */
public interface TaskTypeService {

    /**
     * 查询所有
     *
     * @return TaskTypeVO
     */
    List<TaskTypeVO> getAll();
}
