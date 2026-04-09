package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TableName("cluster_node")
@ApiModel(value = "集群节点")
public class ClusterNodePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    private String name;
    private String ip;
    private String status;
    private Integer cpuCores;
    private Integer gpuCount;
    private String gpuModel;
    private Integer memoryTotal;
    private Integer diskTotal;
}
