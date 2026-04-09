package com.naic.productionline.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.naic.productionline.config.KubernetesConfig;
import com.naic.productionline.config.MlflowConfig;
import com.naic.productionline.domain.dto.AirFlowDTO;
import com.naic.productionline.domain.po.ModelPO;
import com.naic.productionline.domain.po.ModelServicePO;
import com.naic.productionline.mapper.ModelMapper;
import com.naic.productionline.mapper.ModelServiceMapper;
import com.naic.productionline.service.ModelService;
import com.naic.productionline.service.feign.AirflowFeign;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.V1Patch;
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
import java.util.stream.Collectors;

/**
 * 模型部署工具
 *
 * @author wangyunong
 * @date 2024/10/15
 */
@Slf4j
@Component
@AllArgsConstructor
public class ModelServiceUtil {

    private final KubernetesConfig config;

    private final TrainUtil trainUtil;

    private final ModelServiceMapper modelServiceMapper;

    private final ModelMapper modelMapper;

    private final AirflowFeign airflowFeign;

    @Transactional(rollbackFor = Exception.class)
    public void trainDeployment(Long serviceId) throws ApiException {
        ModelServicePO modelService = modelServiceMapper.selectById(serviceId);
        ModelPO modelPO = modelMapper.selectById(modelService.getModelId());
        String trainName = modelService.getId() + "-" + trainUtil.generateRandomName();
        // 加载 Kubernetes 配置
        ApiClient client = new ClientBuilder().setBasePath(config.getBasePath()).
                setVerifyingSsl(false).
                setAuthentication(new AccessTokenAuthentication(config.getToken())).build();
        Configuration.setDefaultApiClient(client);

        // 创建 AppsV1Api 和 CoreV1Api 实例
        AppsV1Api appsApi = new AppsV1Api();
        CoreV1Api coreApi = new CoreV1Api();

        List<String> argList = new ArrayList<>();
        argList.add("--mlflow-model-uri");
        argList.add(modelPO.getModelUrl());

        V1EnvVar env1 = new V1EnvVar();
        env1.setName(config.getEndPointUrlName());
        env1.setValue(config.getEndPointUrl());
        V1EnvVar env2 = new V1EnvVar();
        env2.setName(config.getAccessKeyIdName());
        env2.setValue(config.getAccessKeyId());
        V1EnvVar env3 = new V1EnvVar();
        env3.setName(config.getSecretAccessKeyName());
        env3.setValue(config.getSecretAccessKey());

        List<V1EnvVar> envVarList = new ArrayList<>();
        envVarList.add(env1);
        envVarList.add(env2);
        envVarList.add(env3);

        // 创建 Deployment
        V1Deployment deployment = new V1Deployment()
                .apiVersion("apps/v1")
                .kind("Deployment")
                .metadata(new V1ObjectMeta().name("model-run-" + trainName + "-deployment"))
                .spec(new V1DeploymentSpec()
                        .replicas(1)
                        .selector(new V1LabelSelector().matchLabels(new HashMap<String, String>() {{
                            put("app", "model-run-" + trainName);
                        }}))
                        .template(new V1PodTemplateSpec()
                                .metadata(new V1ObjectMeta().labels(new HashMap<String, String>() {{
                                    put("app", "model-run-" + trainName);
                                }}))
                                .spec(new V1PodSpec()
                                        .containers(java.util.Arrays.asList(new V1Container()
                                                .name("model-run-" + trainName + "-container")
                                                .args(argList)
                                                .image(modelPO.getServiceImage())
                                                .env(envVarList)
                                                .ports(java.util.Arrays.asList(new V1ContainerPort().containerPort(config.getDeployContainerPort())))
                                                .resources(new V1ResourceRequirements()
                                                        .limits(new HashMap<String, Quantity>() {{
                                                            put("nvidia.com/gpu", new Quantity("1"));
                                                        }})
                                                        .requests(new HashMap<String, Quantity>() {{
                                                            put("nvidia.com/gpu", new Quantity("1"));
                                                        }})))))));

        // 创建 Service
        V1Service service = new V1Service()
                .apiVersion("v1")
                .kind("Service")
                .metadata(new V1ObjectMeta().name("model-run-" + trainName + "-service"))
                .spec(new V1ServiceSpec()
                        .selector(new HashMap<String, String>() {{
                            put("app", "model-run-" + trainName);
                        }})
                        .ports(java.util.Arrays.asList(new V1ServicePort()
                                .protocol("TCP")
                                .port(config.getDeployContainerPort())
                        ))
                        .type("NodePort"));

        // 创建 Deployment
        appsApi.createNamespacedDeployment(config.getNameSpace(), deployment, null, null, null, null);

        // 创建 Service
        V1Service ser = coreApi.createNamespacedService(config.getNameSpace(), service, null, null, null, null);

        modelService.setServiceName("model-run-" + trainName);
        modelService.setServiceUrl(config.getPointPath() + ":" + ser.getSpec().getPorts().get(0).getNodePort());
        modelServiceMapper.updateById(modelService);
    }

    /**
     * 删除service
     */
    public void deleteService(String serviceName) {
        Map<String, Object> map = new HashMap<>();
        map.put("service_name", serviceName);
        AirFlowDTO dto = new AirFlowDTO()
                .setDagRunId(UUID.randomUUID().toString())
                .setConf(map);
        airflowFeign.deleteService(dto);
    }

    public void quitOrContinueDeployment(String deploymentName, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("service_name", deploymentName);
        //0中断 1继续
        map.put("replica_count", status);
        AirFlowDTO dto = new AirFlowDTO()
                .setDagRunId(UUID.randomUUID().toString())
                .setConf(map);
        airflowFeign.scaleService(dto);
    }
}
