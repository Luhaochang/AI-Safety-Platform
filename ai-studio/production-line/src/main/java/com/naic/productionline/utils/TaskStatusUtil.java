package com.naic.productionline.utils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.naic.api.service.RedisService;
import com.naic.productionline.config.KubernetesConfig;
import com.naic.productionline.domain.po.AutoLabelPO;
import com.naic.productionline.domain.po.JupyterServicePO;
import com.naic.productionline.domain.po.ModelServicePO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.JobStatusVO;
import com.naic.productionline.mapper.AutoLabelMapper;
import com.naic.productionline.mapper.JupyterServiceMapper;
import com.naic.productionline.mapper.ModelServiceMapper;
import com.naic.productionline.mapper.PipelineTaskMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 更新任务状态
 *
 * @author wangyunong
 * @date 2024/10/11
 */
@Slf4j
@Component
@EnableAsync
@AllArgsConstructor
public class TaskStatusUtil {

    private final PipelineTaskMapper pipelineTaskMapper;

    private final TrainUtil trainUtil;

    private final MLflowUtil mLflowUtil;

    private final ModelServiceMapper modelServiceMapper;

    private final JupyterServiceMapper jupyterServiceMapper;

    private final AutoLabelMapper autoLabelMapper;

    private final KubernetesConfig config;
    /**
     * 更新任务状态
     */
    @PostConstruct
    private void updateTask() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("demo-pool-%d").build();


        ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        singleThreadPool.execute(()-> {
            while (true){
                try {
                    Thread.sleep(2000);

                    List<PipelineTaskPO> taskResultList=pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>().eq("status",2));
                    List<AutoLabelPO> autoLabelList=autoLabelMapper.selectList(new QueryWrapper<AutoLabelPO>().eq("status",2));
                    for (PipelineTaskPO task:taskResultList){
                        JobStatusVO status=trainUtil.getStatus(task.getJobName(), task.getNameSpace());
                        if(status!=null){
                            if((status.getSucceeded()!=null)&&(status.getSucceeded().equals(1))){
                                task.setStatus(4);
                                pipelineTaskMapper.updateById(task);
                            }
                            if((status.getFailed()!=null)&&(status.getFailed().equals(1))){
                                task.setStatus(5);
                                pipelineTaskMapper.updateById(task);
                            }
                        }
                    }
                    for (AutoLabelPO autoLabel:autoLabelList){
                        JobStatusVO status=trainUtil.getStatus(autoLabel.getJobName(), autoLabel.getNameSpace());
                        if(status!=null){
                            if((status.getSucceeded()!=null)&&(status.getSucceeded().equals(1))){
                                autoLabel.setStatus(3);
                                autoLabelMapper.updateById(autoLabel);
                            }
                            if((status.getFailed()!=null)&&(status.getFailed().equals(1))){
                                autoLabel.setStatus(4);
                                autoLabelMapper.updateById(autoLabel);
                            }
                        }
                    }

                    List<PipelineTaskPO> taskTimeList=pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>().eq("status",4).eq("train_time",0));
                    for (PipelineTaskPO task:taskTimeList){
                        task.setTrainTime(mLflowUtil.getRunTime(task.getExperimentName()));

                        pipelineTaskMapper.updateById(task);
                    }

                    List<PipelineTaskPO> taskStartList=pipelineTaskMapper.selectList(new QueryWrapper<PipelineTaskPO>().eq("status",1));
                    List<AutoLabelPO> autoLabelStartList=autoLabelMapper.selectList(new QueryWrapper<AutoLabelPO>().eq("status",1));
                    for (PipelineTaskPO task:taskStartList){
                        String experimentId=mLflowUtil.getExperimentId(task.getExperimentName());
                        if(experimentId!=null){
                            task.setStatus(2);
                            pipelineTaskMapper.updateById(task);
                        }
                    }
                    for (AutoLabelPO autoLabel:autoLabelStartList){
                        if(trainUtil.isExistJob(autoLabel.getJobName(), config.getNameSpace())){
                            autoLabel.setStatus(2);
                            autoLabelMapper.updateById(autoLabel);
                        }
                    }

                    List<ModelServicePO> serviceList=modelServiceMapper.selectList(new QueryWrapper<ModelServicePO>().ne("status",4));
                    for(ModelServicePO service:serviceList){
                        Integer serviceStatus=trainUtil.deploymentStatus(service.getServiceName());
                        service.setStatus(serviceStatus);
                        modelServiceMapper.updateById(service);
                    }

                    List<JupyterServicePO> jupyterServiceList=jupyterServiceMapper.selectList(new QueryWrapper<JupyterServicePO>().ne("status",4));
                    for(JupyterServicePO service:jupyterServiceList){
                        Integer serviceStatus=trainUtil.deploymentStatus(service.getServiceName());
                        service.setStatus(serviceStatus);
                        jupyterServiceMapper.updateById(service);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        singleThreadPool.shutdown();
    }

}
