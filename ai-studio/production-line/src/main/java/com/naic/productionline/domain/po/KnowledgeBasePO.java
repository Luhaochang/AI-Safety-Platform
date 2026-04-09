package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("knowledge_base")
@ApiModel(value = "RAG知识库")
public class KnowledgeBasePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    private String name;
    private String description;
    private Integer documentCount;
    private Long wordCount;
    private String createUser;
    private Date createTime;
    private Date updateTime;
}
