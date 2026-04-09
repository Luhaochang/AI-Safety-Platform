package com.naic.productionline.utils;

import com.naic.productionline.config.KubernetesConfig;
import com.naic.productionline.domain.dto.AirFlowDTO;
import com.naic.productionline.domain.po.*;
import com.naic.productionline.mapper.*;
import com.naic.productionline.service.feign.AirflowFeign;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * jupyter部署工具
 *
 * @author wangyunong
 * @date 2024/10/15
 */
@Slf4j
@Component
@AllArgsConstructor
public class JupyterUtil {

    private final KubernetesConfig config;

    private final TrainUtil trainUtil;

    private final JupyterImageMapper jupyterImageMapper;

    private final JupyterServiceMapper jupyterServiceMapper;

    private final ModelMapper modelMapper;

    private final AirflowFeign airflowFeign;

    private final DataSetMapper dataSetMapper;

    @Transactional(rollbackFor = Exception.class)
    public void trainDeployment(Long jupyterServiceId) throws ApiException {

        JupyterServicePO servicePO=jupyterServiceMapper.selectById(jupyterServiceId);
        JupyterImage jupyterImage= jupyterImageMapper.selectById(servicePO.getJupyterId());
        String trainName=servicePO.getId()+"-"+trainUtil.generateRandomName();
        DataSetPO dataSet= dataSetMapper.selectById(servicePO.getDatasetId());
        // 加载 Kubernetes 配置
        ApiClient client = new ClientBuilder().setBasePath(config.getBasePath()).
                setVerifyingSsl(false).
                setAuthentication(new AccessTokenAuthentication(config.getToken())).build();
        Configuration.setDefaultApiClient(client);

        // 创建 AppsV1Api 和 CoreV1Api 实例
        AppsV1Api appsApi = new AppsV1Api();
        CoreV1Api coreApi = new CoreV1Api();

        Map<String, Quantity> storageMap=new HashMap<>();
        storageMap.put("storage", new Quantity("10Gi"));

        V1PersistentVolumeSpec v1PersistentVolumeSpec=new V1PersistentVolumeSpec()
                .capacity(storageMap)
                .hostPath(new V1HostPathVolumeSource()
                        .path(config.getDatasetConfig()+"/"+dataSet.getFileUrl())
                        .type("DirectoryOrCreate"));
        v1PersistentVolumeSpec.setAccessModes(Arrays.asList("ReadOnlyMany"));

        // 创建 PersistentVolume
        V1PersistentVolume pv = new V1PersistentVolume()
                .metadata(new V1ObjectMeta().name("jupyter-run-"+trainName+"dataset-pv"))
                .spec(v1PersistentVolumeSpec);

        // 创建 PersistentVolumeClaim
        V1PersistentVolumeClaim pvc = new V1PersistentVolumeClaim()
                .metadata(new V1ObjectMeta().name("jupyter-run-"+trainName+"dataset-pvc"))
                .spec(new V1PersistentVolumeClaimSpec()
                        .accessModes(Arrays.asList("ReadOnlyMany"))
                        .resources(new V1ResourceRequirements()
                                .requests(storageMap)));


        // 创建 Deployment
        V1Deployment deployment = new V1Deployment()
                .apiVersion("apps/v1")
                .kind("Deployment")
                .metadata(new V1ObjectMeta().name("jupyter-run-"+trainName+"-deployment"))
                .spec(new V1DeploymentSpec()
                        .replicas(1)
                        .selector(new V1LabelSelector().matchLabels(new HashMap<String, String>() {{
                            put("app", "jupyter-run-"+trainName);
                        }}))
                        .template(new V1PodTemplateSpec()
                                .metadata(new V1ObjectMeta().labels(new HashMap<String, String>() {{
                                    put("app", "jupyter-run-"+trainName);
                                }}))
                                .spec(new V1PodSpec()
                                        .containers(Arrays.asList(new V1Container()
                                                .name("jupyter-run-"+trainName+"-container")
                                                .image(jupyterImage.getFileUrl())
                                                .ports(Arrays.asList(new V1ContainerPort().containerPort(config.getJupyterDeployContainerPort())))
                                                .resources(jupyterImage.getGpuRequired().equals(1)?new V1ResourceRequirements()
                                                        .limits(new HashMap<String, Quantity>() {{
                                                            put("nvidia.com/gpu", new Quantity("1"));
                                                        }})
                                                        .requests(new HashMap<String, Quantity>() {{
                                                            put("nvidia.com/gpu", new Quantity("1"));
                                                        }}):null)
                                                .command(java.util.Arrays.asList("sh", "-c", "jupyter lab --notebook-dir=/ --ip=0.0.0.0 --no-browser --allow-root --port=3000 --NotebookApp.token='' --NotebookApp.password='' --NotebookApp.allow_origin='*'"))
                                                .imagePullPolicy("IfNotPresent")
                                                .volumeMounts(Collections.singletonList(new V1VolumeMount()
                                                        .mountPath(config.getDatasetConfig())
                                                        .name("jupyter-run-"+trainName+"dataset-volume")))
                                        ))
                                        .volumes(Collections.singletonList(new V1Volume()
                                                .name("jupyter-run-"+trainName+"dataset-volume")
                                                .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName("jupyter-run-"+trainName+"dataset-pvc"))))

                                )));

        // 创建 Service
        V1Service service = new V1Service()
                .apiVersion("v1")
                .kind("Service")
                .metadata(new V1ObjectMeta().name("jupyter-run-"+trainName+"-service").namespace(config.getNameSpace()))
                .spec(new V1ServiceSpec()
                        .selector(new HashMap<String, String>() {{
                            put("app", "jupyter-run-"+trainName);
                        }})
                        .ports(Arrays.asList(new V1ServicePort()
                                .protocol("TCP")
                                .port(config.getJupyterDeployContainerPort())
                        ))
                        .type("NodePort"));


        // 创建 PV
        coreApi.createPersistentVolume(pv, null, null, null,null);

        // 创建 PVC
        coreApi.createNamespacedPersistentVolumeClaim(config.getNameSpace(), pvc, null, null, null,null);

        // 创建 Deployment
        appsApi.createNamespacedDeployment(config.getNameSpace(), deployment, null, null, null,null);

        // 创建 Service
        V1Service ser=coreApi.createNamespacedService(config.getNameSpace(), service, null, null, null,null);

        servicePO.setServiceName("jupyter-run-"+trainName);
        servicePO.setServiceUrl(config.getNodePortBasePath()+":"+ser.getSpec().getPorts().get(0).getNodePort());
        servicePO.setPersistentVolumeName("jupyter-run-"+trainName+"dataset-pv");
        servicePO.setPersistentVolumeClaimName("jupyter-run-"+trainName+"dataset-pvc");
        jupyterServiceMapper.updateById(servicePO);
    }

    /**
     * 删除service
     */
    public void deleteService(String serviceName){
        Map<String,Object> map=new HashMap<>();
        map.put("service_name",serviceName);
        AirFlowDTO dto=new AirFlowDTO()
                .setDagRunId(UUID.randomUUID().toString())
                .setConf(map);
        airflowFeign.deleteService(dto);
    }

    /**
     * 删除pv,pvc
     */
    public void deletePersistentVolume(String pvName,String pvcName) throws ApiException {
        ApiClient client = new ClientBuilder().setBasePath(config.getBasePath()).
                setVerifyingSsl(false).
                setAuthentication(new AccessTokenAuthentication(config.getToken())).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        api.deleteNamespacedPersistentVolumeClaim(pvcName, config.getNameSpace(), null, null, null,null,null, deleteOptions);
        api.deletePersistentVolume(pvName, null, null,null,null,null, deleteOptions);
    }
}
