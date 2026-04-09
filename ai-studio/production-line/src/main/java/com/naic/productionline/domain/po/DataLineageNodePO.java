package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TableName("data_lineage_node")
@ApiModel(value = "数据血缘节点")
public class DataLineageNodePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    private String name;
    private String type;
    private String layer;
    private String grade;
}
