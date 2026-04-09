package com.naic.productionline.utils;

import com.naic.productionline.config.KubernetesConfig;
import com.naic.productionline.domain.dto.AirFlowDTO;
import com.naic.productionline.domain.vo.JobStatusVO;
import com.naic.productionline.domain.vo.homePage.NodeStatistics;
import com.naic.productionline.service.feign.AirflowFeign;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.util.Config;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 训练工具类
 * 负责与 Kubernetes 交互，管理 Job、ConfigMap 及获取节点状态
 *
 * @author wangyunong
 * @date 2024/10/10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainUtil {

    private final KubernetesConfig config;
    private final AirflowFeign airflowFeign;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int STRING_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String MLFLOW_EXPERIMENT_NAME = "MLFLOW_EXPERIMENT_NAME";
    private static final String DATASET_MOUNT_NAME = "dataset-mount";
    private static final String API_VERSION_BATCH_V1 = "batch/v1";
    private static final String KIND_JOB = "Job";
    private static final String RESTART_POLICY_ON_FAILURE = "OnFailure";
    private static final String VOLUME_TYPE_DIRECTORY_OR_CREATE = "DirectoryOrCreate";

    /**
     * 提交训练任务 Job
     */
    public void train(Map<String, String> map, String nameSpace,
                      String jobName, String containerName,
                      String imageName, String configMapName,
                      String experimentName) throws ApiException {

        // 1. 创建 ConfigMap
        createConfigMap(map, configMapName, nameSpace);

        // 2. 构建 Job 对象
        V1Job job = buildJob(jobName, containerName, imageName, configMapName, experimentName, map);

        // 3. 提交 Job
        BatchV1Api api = new BatchV1Api(getApiClient());
        api.createNamespacedJob(nameSpace, job, null, null, null, null);
        log.info("Successfully created Job [{}] in namespace [{}]", jobName, nameSpace);
    }

    /**
     * 获取 Job 状态
     */
    public JobStatusVO getStatus(String jobName, String nameSpace) {
        try {
            BatchV1Api api = new BatchV1Api(getApiClient());
            // 使用 readNamespacedJob 直接获取，比 list 更高效
            V1Job job = api.readNamespacedJob(jobName, nameSpace, null);

            JobStatusVO vo = new JobStatusVO();
            vo.setJobName(jobName);
            if (job.getStatus() != null) {
                vo.setActive(job.getStatus().getActive());
                vo.setSucceeded(job.getStatus().getSucceeded());
                vo.setFailed(job.getStatus().getFailed());
            }
            return vo;
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.warn("Job [{}] not found in namespace [{}]", jobName, nameSpace);
                return null;
            }
            log.error("Error getting status for job [{}]", jobName, e);
            return null;
        }
    }

    /**
     * 删除 Job (通过 Airflow)
     */
    public void deleteJob(String jobName) {
        Map<String, Object> map = new HashMap<>();
        map.put("job_name", jobName);
        AirFlowDTO dto = new AirFlowDTO()
                .setDagRunId(UUID.randomUUID().toString())
                .setConf(map);
        airflowFeign.deleteJob(dto);
        log.info("Triggered deleteJob for [{}] via Airflow", jobName);
    }

    /**
     * 删除 ConfigMap
     */
    public void deleteConfigMap(String configMapName) throws ApiException {
        CoreV1Api api = new CoreV1Api(getApiClient());
        try {
            api.deleteNamespacedConfigMap(configMapName, config.getNameSpace(), null, null, null, null, null, new V1DeleteOptions());
            log.info("Deleted ConfigMap [{}]", configMapName);
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                throw e;
            }
            log.warn("ConfigMap [{}] not found, skipping delete", configMapName);
        }
    }

    /**
     * 创建 ConfigMap
     */
    public void createConfigMap(Map<String, String> data, String configMapName, String nameSpace) throws ApiException {
        CoreV1Api api = new CoreV1Api(getApiClient());
        V1ConfigMap configMap = new V1ConfigMap()
                .metadata(new V1ObjectMeta().name(configMapName))
                .data(data);

        api.createNamespacedConfigMap(nameSpace, configMap, null, null, null, null);
        log.debug("Created ConfigMap [{}] in namespace [{}]", configMapName, nameSpace);
    }

    /**
     * 更新 ConfigMap
     */
    public void updateConfigMap(Map<String, String> data, String configMapName, String nameSpace) throws ApiException {
        CoreV1Api api = new CoreV1Api(getApiClient());
        // 先读取旧的，确保存在
        V1ConfigMap existingConfigMap = api.readNamespacedConfigMap(configMapName, nameSpace, null);
        existingConfigMap.setData(data);
        
        api.replaceNamespacedConfigMap(configMapName, nameSpace, existingConfigMap, null, null, null, null);
        log.debug("Updated ConfigMap [{}] in namespace [{}]", configMapName, nameSpace);
    }

    /**
     * 生成随机名称
     */
    public String generateRandomName() {
        StringBuilder sb = new StringBuilder(STRING_LENGTH);
        for (int i = 0; i < STRING_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 获取 Deployment 状态 (基于 Pod 状态)
     * @return 1: 部署中/Pending, 2: 成功/Running/Succeeded, 3: 失败
     */
    public Integer deploymentStatus(String deploymentName) throws ApiException {
        CoreV1Api api = new CoreV1Api(getApiClient());
        String namespace = config.getNameSpace();
        
        V1PodList podList = api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null);
        
        // 查找名称包含 deploymentName 的 Pod
        Optional<V1Pod> targetPod = podList.getItems().stream()
                .filter(pod -> pod.getMetadata().getName() != null && pod.getMetadata().getName().contains(deploymentName))
                .findFirst();

        if (!targetPod.isPresent()) {
            return 1;
        }

        String phase = targetPod.get().getStatus().getPhase();
        if ("Pending".equals(phase)) {
            return 1;
        } else if ("Running".equals(phase) || "Succeeded".equals(phase)) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * 获取节点统计信息
     */
    public List<NodeStatistics> getNodeStatistics() throws ApiException {
        CoreV1Api api = new CoreV1Api(getApiClient());
        V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null, null);

        return nodeList.getItems().stream()
                .map(this::convertToNodeStatistics)
                .collect(Collectors.toList());
    }

    /**
     * 检查 Job 是否存在
     */
    public Boolean isExistJob(String jobName, String nameSpace) {
        try {
            BatchV1Api api = new BatchV1Api(getApiClient());
            api.readNamespacedJob(jobName, nameSpace, null);
            return false;
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                log.error("Error checking existence of job [{}]", jobName, e);
            }
            return false;
        }
    }

    // ================= Private Helper Methods =================

    private ApiClient getApiClient() {
        ApiClient client = new ClientBuilder()
                .setBasePath(config.getBasePath())
                .setVerifyingSsl(false)
                .setAuthentication(new AccessTokenAuthentication(config.getToken()))
                .build();
        Configuration.setDefaultApiClient(client);
        return client;
    }

    private V1Job buildJob(String jobName, String containerName, String imageName, 
                           String configMapName, String experimentName, Map<String, String> params) {
        V1Job job = new V1Job();
        job.setApiVersion(API_VERSION_BATCH_V1);
        job.setKind(KIND_JOB);
        
        V1ObjectMeta meta = new V1ObjectMeta();
        meta.setName(jobName);
        job.setMetadata(meta);

        V1JobSpec spec = new V1JobSpec();
        spec.setCompletions(1);
        spec.setParallelism(1);
        spec.setBackoffLimit(4);
        spec.setTtlSecondsAfterFinished(600);

        V1PodTemplateSpec templateSpec = new V1PodTemplateSpec();
        V1PodSpec podSpec = new V1PodSpec();
        podSpec.setRestartPolicy(RESTART_POLICY_ON_FAILURE);
        
        V1Container container = buildContainer(containerName, imageName, configMapName, experimentName, params);
        podSpec.setContainers(Collections.singletonList(container));
        podSpec.setVolumes(buildVolumes(configMapName));

        templateSpec.setSpec(podSpec);
        spec.setTemplate(templateSpec);
        job.setSpec(spec);
        
        return job;
    }

    private V1Container buildContainer(String containerName, String imageName, String configMapName, 
                                       String experimentName, Map<String, String> params) {
        V1Container container = new V1Container();
        container.setName(containerName);
        container.setImage(imageName);

        List<String> args = new ArrayList<>();
        if (params != null) {
            params.forEach((k, v) -> {
                args.add("--" + k);
                args.add(v);
            });
        }
        container.setArgs(args);

        container.setVolumeMounts(buildVolumeMounts(configMapName));
        container.setEnv(buildEnvVars(experimentName));

        return container;
    }

    private List<V1VolumeMount> buildVolumeMounts(String configMapName) {
        V1VolumeMount configMount = new V1VolumeMount()
                .name(configMapName)
                .mountPath(config.getConfig());

        V1VolumeMount datasetMount = new V1VolumeMount()
                .name(DATASET_MOUNT_NAME)
                .mountPath(config.getDatasetConfig())
                .readOnly(true);

        return Arrays.asList(configMount, datasetMount);
    }

    private List<V1EnvVar> buildEnvVars(String experimentName) {
        return Arrays.asList(
                new V1EnvVar().name(config.getEndPointUrlName()).value(config.getEndPointUrl()),
                new V1EnvVar().name(config.getAccessKeyIdName()).value(config.getAccessKeyId()),
                new V1EnvVar().name(config.getSecretAccessKeyName()).value(config.getSecretAccessKey()),
                new V1EnvVar().name(MLFLOW_EXPERIMENT_NAME).value(experimentName)
        );
    }

    private List<V1Volume> buildVolumes(String configMapName) {
        V1Volume configVolume = new V1Volume()
                .name(configMapName)
                .configMap(new V1ConfigMapVolumeSource().name(configMapName));

        V1Volume datasetVolume = new V1Volume()
                .name(DATASET_MOUNT_NAME)
                .hostPath(new V1HostPathVolumeSource()
                        .path(config.getDatasetConfig())
                        .type(VOLUME_TYPE_DIRECTORY_OR_CREATE));

        return Arrays.asList(configVolume, datasetVolume);
    }

    private NodeStatistics convertToNodeStatistics(V1Node node) {
        NodeStatistics stats = new NodeStatistics();
        stats.setNodeName(node.getMetadata().getName());
        
        V1NodeStatus status = node.getStatus();
        if (status != null) {
            List<V1NodeAddress> addresses = status.getAddresses();
            if (!CollectionUtils.isEmpty(addresses)) {
                stats.setNodeAddress(addresses.get(0).getAddress());
            }
            
            Map<String, Quantity> allocatable = status.getAllocatable();
            if (allocatable != null) {
                stats.setCpuNumber(getQuantityValue(allocatable.get("cpu")));
                stats.setMemoryNumber(getQuantityValue(allocatable.get("memory")));
                stats.setGpuNumber(getQuantityValue(allocatable.get("nvidia.com/gpu")));
                stats.setPodsNumber(getQuantityValue(allocatable.get("pods")));
            }
        }
        return stats;
    }
    
    private BigDecimal getQuantityValue(Quantity quantity) {
        return quantity != null ? quantity.getNumber() : BigDecimal.ZERO;
    }
}
